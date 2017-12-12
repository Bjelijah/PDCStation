#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <android/log.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include "include/stream_type.h"
#include "include/net_sdk.h"
#include "include/play_def.h"
#include "include/IHW265Dec_Api.h"
#include "include/transmgr.h"
#include "com_howell_jni_JniUtil.h"
#include <unistd.h>
#include <semaphore.h>
#include <ecamstreamreq.h>
#include <g711/g711.h>
#include <protocol_type.h>
#include <stream_type.h>
#include <hw_config.h>
#include <string.h>


#define LOGI(...) (g_debug_enable?(void)__android_log_print(ANDROID_LOG_INFO, "JNI", __VA_ARGS__):(void)NULL)
#define LOGW(...) (g_debug_enable?(void)__android_log_print(ANDROID_LOG_WARN, "JNI", __VA_ARGS__):(void)NULL)
#define LOGE(...) (g_debug_enable?(void)__android_log_print(ANDROID_LOG_ERROR, "JNI", __VA_ARGS__):(void)NULL)



typedef struct{
    char set_time_method_name[32];
    char request_method_name[32];
}yv12_info_t;

typedef struct{
    char * y;
    char * u;
    char * v;
    unsigned long long time;
    int width;
    int height;
    int enable;

    /* multi thread */
    int method_ready;
    JavaVM * jvm;
    JNIEnv * env;
    jmethodID request_render_method,set_time_method;
    jobject callback_obj;
    pthread_mutex_t lock;
    unsigned long long first_time;
}YV12_display_t;

typedef struct AudioPlay
{
    /* multi thread */
    int method_ready,jsonMethod_ready;
    JavaVM * jvm;
    JNIEnv * env;
    jmethodID mid,jsonMid;
    jobject obj;
    jfieldID data_length_id;
    jbyteArray data_array;
    int data_array_len;
    int stop;
}TAudioPlay;
static TAudioPlay* self = NULL;


static yv12_info_t    * g_yuv_info     = NULL;
static YV12_display_t * g_yuv_display  = NULL;

struct StreamResource
{
    JavaVM * jvm;
    JNIEnv * env;
    jobject obj;
    jmethodID mid;
    USER_HANDLE user_handle;
    ALARM_STREAM_HANDLE alarm_stream_handle;
    PLAY_HANDLE play_handle;
    IH265DEC_HANDLE play_265_handle;
    LIVE_STREAM_HANDLE live_stream_handle;
    FILE_STREAM_HANDLE file_stream_handle;
    FILE_LIST_HANDLE file_list_handle,file_list_handle_all;
    int total_file_list_count;
    SYSTEMTIME beg,end; //回放文件的开始结束 不是列表的

    int media_head_len;
    int is_exit;	//退出标记位
    int is_pause;//暂停标志位
    pthread_mutex_t lock_play;
    sem_t sem_play;
    int isFirstTime;
    long firstTimestamp;
    long timestamp;
    int stream_len;

};
static struct StreamResource * res = NULL;

static int g_debug_enable = 0;

typedef struct {
    JavaVM* jvm;
    JNIEnv * env;
    jobject callback_obj;
    jmethodID on_connect_method,on_disconnect_method,on_recordFile_method,on_socket_error_method,on_subscribe_method,on_unsubscribe_method,on_ack_all_method;
    int transDataLen;

}TRANS_T;

static TRANS_T* g_transMgr = NULL;



JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_logEnable
        (JNIEnv *, jclass, jboolean enable){
    g_debug_enable = enable?1:0;
}

/**
 *  Local Fun
 */
void yv12gl_display(const unsigned char * y, const unsigned char *u,const unsigned char *v, int width, int height, unsigned long long time)
{
    if(g_yuv_display == NULL) {LOGE("g_yuv_display==null error");return;}
    if (!g_yuv_display->enable) {LOGE("g_yuv_display enable false error return");return;}
    g_yuv_display->time = time/1000;

    if( g_yuv_display->jvm->AttachCurrentThread(&g_yuv_display->env,NULL)!= JNI_OK) {
        LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
        return;
    }


    // get JAVA method first

    if (!g_yuv_display->method_ready) {
        LOGE("method read");
        jclass cls =  g_yuv_display->env->GetObjectClass(g_yuv_display->callback_obj);
        if (cls == NULL) {
            LOGE("FindClass() Error.....");
            goto error;
        }
        //�ٻ�����еķ���

        g_yuv_display->request_render_method = g_yuv_display->env->GetMethodID(cls,g_yuv_info->request_method_name,"()V");
        g_yuv_display->set_time_method = g_yuv_display->env->GetMethodID(cls,g_yuv_info->set_time_method_name,"(J)V");
        if (g_yuv_display->request_render_method == NULL || g_yuv_display->set_time_method == NULL) {
            LOGE("%d  GetMethodID() Error.....",__LINE__);
            goto error;
        }
        g_yuv_display->method_ready=1;
    }

    g_yuv_display->env->CallVoidMethod(g_yuv_display->callback_obj,g_yuv_display->set_time_method,g_yuv_display->time);
    pthread_mutex_lock(&g_yuv_display->lock);
    if (width!=g_yuv_display->width || height!=g_yuv_display->height) {
        LOGE("width=%d   g_width=%d    height=%d   g_height=%d\n",width,g_yuv_display->width,height,g_yuv_display->height);
        if(g_yuv_display->y!=NULL){
            free(g_yuv_display->y);
            g_yuv_display->y = NULL;
        }
        if(g_yuv_display->u!=NULL){
            free(g_yuv_display->u);
            g_yuv_display->u = NULL;
        }
        if(g_yuv_display->v!=NULL){
            free(g_yuv_display->v);
            g_yuv_display->v = NULL;
        }
        g_yuv_display->y = (char *)realloc(g_yuv_display->y,width*height);
        g_yuv_display->u = (char *)realloc(g_yuv_display->u,width*height/4);
        g_yuv_display->v = (char *)realloc(g_yuv_display->v,width*height/4);
        g_yuv_display->width = width;
        g_yuv_display->height = height;
    }
   // LOGI("cpy y yv12gl_display");
    memcpy(g_yuv_display->y,y,width*height);
    memcpy(g_yuv_display->u,u,width*height/4);
    memcpy(g_yuv_display->v,v,width*height/4);
    pthread_mutex_unlock(&g_yuv_display->lock);

    g_yuv_display->env->CallVoidMethod(g_yuv_display->callback_obj,g_yuv_display->request_render_method,NULL);

    if (g_yuv_display->jvm->DetachCurrentThread() != JNI_OK) {
        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
    }
    return;

    error:
    if (g_yuv_display->jvm->DetachCurrentThread() != JNI_OK) {
        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
    }

    return;
}

void audio_play(const char* buf,int len)
{
    if(self==NULL){
        LOGE("self==NULL");
        return;
    }
    if (self->stop) {
        LOGE("self.stop =1");
        return;
    }
    if(self->obj == NULL){

        return;
    }
    if (self->jvm->AttachCurrentThread( &self->env, NULL) != JNI_OK) {
        LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
        return;
    }
    /* get JAVA method first */
    if (!self->method_ready) {


        jclass cls;
        cls = self->env->GetObjectClass(self->obj);
        if (cls == NULL) {
            LOGE("FindClass() Error.....");
            goto error;
        }
        //�ٻ�����еķ���
        self->mid = self->env->GetMethodID(cls, "audioWrite", "()V");
        if (self->mid == NULL) {
            LOGE("GetMethodID() Error.....");
            goto error;
        }

        self->method_ready=1;
    }
    /* update length */
    self->env->SetIntField(self->obj,self->data_length_id,len);
    /* update data */
    if (len<=self->data_array_len) {
        self->env->SetByteArrayRegion(self->data_array,0,len,(const signed char *)buf);
        /* notify the JAVA */
        self->env->CallVoidMethod( self->obj, self->mid, NULL);
    }

   // LOGE("start to detach audio play thread");

    if (self->jvm->DetachCurrentThread() != JNI_OK) {
        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
    }else{
     //   LOGE("audio_play detach ok");
    }
    /* char* data = (*self.env)->GetByteArrayElements(self.env,self.data_array,0); */
    /* memcpy(data,buf,len); */
    return;

    error:
    if (self->jvm->DetachCurrentThread() != JNI_OK) {
        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
    }
}





////////////////////////////////////////////////////////
////////////////////////////////////////////////////////
/**
 *  JNI interface
 */
JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVInit
        (JNIEnv *env, jclass){
    if(g_yuv_info == NULL){
        g_yuv_info = (yv12_info_t*)malloc(sizeof(yv12_info_t));
        memset(g_yuv_info,0,sizeof(yv12_info_t));
    }
    if(g_yuv_display == NULL){
        g_yuv_display = (YV12_display_t*)malloc(sizeof(YV12_display_t));
        memset(g_yuv_display,0,sizeof(YV12_display_t));
        env->GetJavaVM(&g_yuv_display->jvm);
        //FIXME  now obj=JniYV12Util should be YV12Renderer
        pthread_mutex_init(&g_yuv_display->lock,NULL);
        g_yuv_display->width  = 352;
        g_yuv_display->height = 288;
        g_yuv_display->y = (char*)malloc(g_yuv_display->width*g_yuv_display->height);
        g_yuv_display->u = (char*)malloc(g_yuv_display->width*g_yuv_display->height/4);
        g_yuv_display->v = (char*)malloc(g_yuv_display->width*g_yuv_display->height/4);
        memset(g_yuv_display->y,0,g_yuv_display->width*g_yuv_display->height);//4:2:2 black
        memset(g_yuv_display->u,128,g_yuv_display->width*g_yuv_display->height/4);
        memset(g_yuv_display->v,128,g_yuv_display->width*g_yuv_display->height/4);
    }else{
        memset(g_yuv_display->y,0,g_yuv_display->width*g_yuv_display->height);//4:2:2 black
        memset(g_yuv_display->u,128,g_yuv_display->width*g_yuv_display->height/4);
        memset(g_yuv_display->v,128,g_yuv_display->width*g_yuv_display->height/4);
    }
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVDeinit
        (JNIEnv *env, jclass){
    return;//we never release// FIXME
    if(g_yuv_info!=NULL){
        free(g_yuv_info);
        g_yuv_info = NULL;
    }
    if(g_yuv_display!=NULL){
        if(g_yuv_display->callback_obj!=NULL){
            env->DeleteGlobalRef(g_yuv_display->callback_obj);
        }
        if(g_yuv_display->y!=NULL){
            free(g_yuv_display->y);
            g_yuv_display->y = NULL;
        }
        if(g_yuv_display->u!=NULL){
            free(g_yuv_display->u);
            g_yuv_display->u = NULL;
        }
        if(g_yuv_display->v!=NULL){
            free(g_yuv_display->v);
            g_yuv_display->v = NULL;
        }
        pthread_mutex_destroy(&g_yuv_display->lock);
        free(g_yuv_display);
        g_yuv_display = NULL;
    }
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVSetCallbackObject
        (JNIEnv *env, jclass, jobject callbackObject, jint flag){
    if(g_yuv_info==NULL)return;
    switch (flag) {
        case 0:
            g_yuv_display->callback_obj = env->NewGlobalRef(callbackObject);
            break;
        default:
            break;
    }
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVSetCallbackMethodName
        (JNIEnv *env, jclass, jstring method_name, jint flag){
    if(g_yuv_info==NULL)return;
    const char * _method_name= env->GetStringUTFChars(method_name,NULL);
    switch (flag) {
        case 0:
            strcpy(g_yuv_info->set_time_method_name,_method_name);
            break;
        case 1:
            strcpy(g_yuv_info->request_method_name,_method_name);
            break;
        default:
            break;
    }
    env->ReleaseStringUTFChars(method_name,_method_name);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVLock
        (JNIEnv *, jclass){
    if(g_yuv_display == NULL){
        return;
    }
    pthread_mutex_lock(&g_yuv_display->lock);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVUnlock
        (JNIEnv *, jclass){
    if(g_yuv_display == NULL){
        return;
    }
    pthread_mutex_unlock(&g_yuv_display->lock);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVSetEnable
        (JNIEnv *, jclass){
    g_yuv_display->enable = 1;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVRenderY
        (JNIEnv *, jclass){
    if (g_yuv_display->y == NULL) {
        char value[4] = {0,0,0,0};
        glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,2,2,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,value);
    }
    else {
        glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,g_yuv_display->width,g_yuv_display->height,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,g_yuv_display->y);
    }
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVRenderU
        (JNIEnv *, jclass){
    if (g_yuv_display->u == NULL) {
        char value[] = {128};
        glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,1,1,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,value);
    }
    else {
        glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,g_yuv_display->width/2,g_yuv_display->height/2,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,g_yuv_display->u);
    }
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVRenderV
        (JNIEnv *, jclass){
    if (g_yuv_display->v == NULL) {
        char value[] = {128};
        glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,1,1,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,value);
    }
    else {
        glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,g_yuv_display->width/2,g_yuv_display->height/2,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,g_yuv_display->v);
    }
}
int g_num = 0;
char * g_h264Buffer = NULL;
void inputHWData2HWPlayer(const unsigned char *data,int len){
    if(res==NULL){
        return;
    }
    if(res->is_exit){
        return;
    }

//	int ret = hwplay_input_data(res->play_handle,(const char*) data ,len);
//	LOGI("input_data ret=%d",ret);





    pthread_mutex_lock(&res->lock_play);

    int num = 0;
    int ret = 0;

    int remain = 0;
    int stream_buf_len = 0;
    hwplay_get_stream_buf_len(res->play_handle,&stream_buf_len);

    do{
        hwplay_get_stream_buf_remain(res->play_handle,&remain);
        LOGI("remain=%d  len=%d\n",remain,len);
        if(remain>len){
            break;
        }
        usleep(40000);
        g_num++;
    }while(g_num<50);
    hwplay_get_stream_buf_remain(res->play_handle,&remain);
    LOGI("before   remain=%d  len=%d\n",remain,len);
    ret = hwplay_input_data(res->play_handle,(const char*)  data ,len);
    hwplay_get_stream_buf_remain(res->play_handle,&remain);
    LOGI("after   ret=%d  remain=%d  len=%d\n",ret,remain,len);
    pthread_mutex_unlock(&res->lock_play);
    g_num = 0;
}


JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_YUVsetData
        (JNIEnv *env, jclass,jbyteArray data,jint len,jint w,jint h){
    char *buf = (char *)env->GetByteArrayElements(data,0);
    unsigned char* y = (unsigned char *)buf;
    unsigned char* u = y+w*h;
    unsigned char* v = u+w*h/4;
    LOGI("yuv set data");
    yv12gl_display(y,u,v,w,h,0);
    env->ReleaseByteArrayElements(data,(signed char*)buf,0);
}


JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_setH264Data
        (JNIEnv *env, jclass,jbyteArray h264,jint len,jint w,jint h,jint isI){
    char *buf = (char *)env->GetByteArrayElements(h264,0);
    stream_head hw_head;
    memset(&hw_head,0,sizeof(stream_head));
    hw_head.tag = HW_MEDIA_TAG;
    hw_head.sys_time = time(NULL);
    hw_head.len = sizeof(stream_head) + len;
    hw_head.type = isI ? 1: 0;
    struct timeval val;
    gettimeofday(&val,NULL);
    uint64_t timestamp = (unsigned long long)val.tv_sec * 1000 + val.tv_usec / 1000;
    hw_head.time_stamp = timestamp *1000;
    if(g_h264Buffer==NULL){
        g_h264Buffer =(char *) malloc(2*1024*1024);
        memset(g_h264Buffer,0,2*1024*1024);
    }
    memcpy(g_h264Buffer,&hw_head,sizeof(stream_head));
    memcpy(g_h264Buffer+sizeof(stream_head),buf,len);
    inputHWData2HWPlayer((const unsigned char*)g_h264Buffer,hw_head.len);
    env->ReleaseByteArrayElements(h264,(signed char*)buf,0);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_setHWData
        (JNIEnv *env, jclass,jbyteArray data,jint len){
    char *buf = (char *)env->GetByteArrayElements(data,0);
    inputHWData2HWPlayer((const unsigned char*)buf,len);
    env->ReleaseByteArrayElements(data,(signed char*)buf,0);
}

JNIEXPORT jbyteArray JNICALL Java_com_howell_jni_JniUtil_H264toHWStream
        (JNIEnv *env, jclass,jbyteArray h264,jint len,jint isI){
    char *buf = (char *)env->GetByteArrayElements(h264,0);
    stream_head hw_head;
    memset(&hw_head,0,sizeof(stream_head));
    hw_head.tag = HW_MEDIA_TAG;
    hw_head.sys_time = time(NULL);
    hw_head.len = sizeof(stream_head) + len;
    hw_head.type = isI ? 1: 0;
    struct timeval val;
    gettimeofday(&val,NULL);
    uint64_t timestamp = (unsigned long long)val.tv_sec * 1000 + val.tv_usec / 1000;
    hw_head.time_stamp = timestamp *1000;
    jbyteArray array = env->NewByteArray(hw_head.len);
    char *hwBuf = (char *)env->GetByteArrayElements(array,0);
    memcpy(hwBuf,&hw_head,sizeof(stream_head));
    memcpy(hwBuf+sizeof(stream_head),buf,len);
    env->ReleaseByteArrayElements(h264,(signed char*)buf,0);
    env->ReleaseByteArrayElements(array,(signed char*)hwBuf,0);
    return array;
}









///////////////////////////////////////////
//////////////////////////////////////////
/////////////////////////////////////////////

/**
 *  net
 */
//192.168.18.23







static int register_nvr(const char* ip){
    //	int ret = hwnet_init(5888);
    //	/* 192.168.18.23 */
    //	LOGI("ret=%d    ip=%s   ",ret,ip);
    	res->user_handle = hwnet_login(ip,5198,"admin","12345");
    //	if(res->user_handle == -1){
    //		LOGE("hwnet_login fail");
    //		return 0;
    //	}
    return res->user_handle>=0?1:0;
}

void *HW265D_Malloc(UINT32 channel_id, UINT32 size)
{
    return (void *)malloc(size);
}

void HW265D_Free(UINT32 channel_id, void * ptr)
{
    free(ptr);
}

void HW265D_Log( UINT32 channel_id, IHWVIDEO_ALG_LOG_LEVEL eLevel, INT8 *p_msg, ...)
{
}
#if 0
void initHi265Decode(){
    if(res == NULL)return;
    IHW265D_INIT_PARAM stInitParam = {0};
    IH265DEC_INARGS stInArgs;
    IH265DEC_OUTARGS stOutArgs = {0};
    IHWVIDEO_ALG_VERSION_STRU stVersion;

    INT32 MultiThreadEnable = 0;	// default is single thread mode
    INT32 DispOutput = 0;

    stInitParam.uiChannelID = 0;
    stInitParam.iMaxWidth   = 1920;
    stInitParam.iMaxHeight  = 1088;
    stInitParam.iMaxRefNum  = 4;

    stInitParam.eThreadType = MultiThreadEnable? IH265D_MULTI_THREAD: IH265D_SINGLE_THREAD;
    stInitParam.eOutputOrder= DispOutput? IH265D_DISPLAY_ORDER:IH265D_DECODE_ORDER;

    stInitParam.MallocFxn  = HW265D_Malloc;
    stInitParam.FreeFxn    = HW265D_Free;
    stInitParam.LogFxn     = HW265D_Log;

    INT32 iRet = 0;
    iRet = IHW265D_Create(&res->play_265_handle, &stInitParam);
    if (IHW265D_OK != iRet)
    {
        LOGE("265 create error");
    }

}

void deInit265Decode(){
    if(res==NULL)return;
    IHW265D_Delete(res->play_265_handle);
}



INT32 H265DecLoadAU(UINT8* pStream, UINT32 iStreamLen, UINT32* pFrameLen)
{
    UINT32 i;
    UINT32 state = 0xffffffff;
    BOOL32 bFrameStartFound=0;
    BOOL32 bSliceStartFound = 0;

    *pFrameLen = 0;
    if( NULL == pStream || iStreamLen <= 4)
    {
        return -1;
    }

    for( i = 0; i < iStreamLen; i++)
    {
        if( (state & 0xFFFFFF7E) >= 0x100 &&
            (state & 0xFFFFFF7E) <= 0x13E )
        {
            if( 1 == bFrameStartFound || bSliceStartFound == 1 )
            {
                if( (pStream[i+1]>>7) == 1)
                {
                    *pFrameLen = i - 4;
                    return 0;
                }
            }
            else
            {
                bSliceStartFound = 1;
                //bFrameStartFound = 1;
            }
        }

        /*find a vps, sps, pps*/
        if( (state&0xFFFFFF7E) == 0x140 ||
            (state&0xFFFFFF7E) == 0x142 ||
            (state&0xFFFFFF7E) == 0x144)
        {
            if (1 == bSliceStartFound)
            {
                bSliceStartFound = 1;
            }
            else if(1 == bFrameStartFound)
            {
                *pFrameLen = i - 4;
                return 0;
            }
            else
            {
                bFrameStartFound = 1;
            }
        }

        state = (state << 8) | pStream[i];
    }

    *pFrameLen = i;
    return -1;
}




void hi265InputData(const char *buf,int len){

    INT32 iNaluLen;
    H265DecLoadAU((UINT8 *)buf, (UINT32)len, (UINT32*)&iNaluLen);
}

#endif
















void on_live_stream_fun(LIVE_STREAM_HANDLE handle,int stream_type,const char* buf,int len,long userdata){
    //__android_log_print(ANDROID_LOG_INFO, "jni", "-------------stream_type %d-len %d",stream_type,len);
    res->stream_len += len;
    if(res == NULL){
        LOGE("on live stream_fun res==null error return");
        return;
    }
    if(res->is_exit == 1){
        LOGE("on live stream_fun is exit return");
        return;
    }
    //fixme 阻塞
//    pthread_mutex_lock(&res->lock_play);

    int ret = hwplay_input_data(res->play_handle, buf ,len);
//    pthread_mutex_unlock(&res->lock_play);
    //	LOGI("on live stream fun len=%d",len);

    //hi265InputData(buf,len);

}

void on_file_stream_fun(FILE_STREAM_HANDLE handle,const char *buf,int len,long userdata){
    res->stream_len += len;
    if(res == NULL){
        LOGE("on file stream_fun res==NULL error");
        return;
    }
    if(res->is_exit == 1){
        LOGE("on file stream is exit return");
        return;
    }

    if (res->is_pause){
        sem_wait(&res->sem_play);
    }


    int ret = hwplay_input_data(res->play_handle, buf ,len);
//    LOGI("on_file_stream_fun input data ret=%d",ret);
}



static void on_source_callback(PLAY_HANDLE handle, int type, const char* buf, int len, unsigned long timestamp, long sys_tm, int w, int h, int framerate, int au_sample, int au_channel, int au_bits, long user){

//    LOGE("on source_callback       type=%d  len=%d  w=%d  h=%d  timestamp=%ld sys_tm=%ld  framerate=%d  au_sample=%d  au_channel=%d au_bits=%d",type,len,w,h,timestamp,sys_tm,framerate,au_sample,au_channel,au_bits);
//    int ret = hwplay_is_pause(handle);
//    LOGE("on source callback is pause=%d\n",ret);
    if (res!=NULL){
        if (res->is_exit){
            LOGE("on source callback is  exit return");
            return;
        }
    }


    if (res!=NULL){
        if (res->isFirstTime){
            res->isFirstTime = 0;
            res->firstTimestamp = timestamp;
        }
        res->timestamp = timestamp;
    }


    if(type == 0){//音频

        //		audio_play(buf,len,au_sample,au_channel,au_bits);
        audio_play(buf, len);//add cbj
    }else if(type == 1){//视频

        unsigned char* y = (unsigned char *)buf;
        unsigned char* u = y+w*h;
        unsigned char* v = u+w*h/4;
      //  LOGI("on source callback  yv12 display  timestamp=%d\n",timestamp);

        yv12gl_display(y,u,v,w,h,timestamp);
    }
}


JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_netInit
        (JNIEnv *env, jclass cls){
    if(res == NULL){
        res = (StreamResource *)malloc(sizeof(StreamResource));
        memset(res,0,sizeof(StreamResource));
        env->GetJavaVM(&res->jvm);
        res->obj = NULL;
        res->is_exit = 0;
        res->play_handle = -1;
        res->isFirstTime = 1;
        res->firstTimestamp = 0;
        res->timestamp = 0;
        res->file_list_handle = -1;
        res->stream_len = 0;
        res->live_stream_handle = -1;
        res->file_stream_handle = -1;
        res->total_file_list_count = -1;
        res->file_list_handle_all = -1;
        pthread_mutex_init(&res->lock_play,NULL);
        sem_init(&res->sem_play,0,0);
    }

}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_netDeinit
        (JNIEnv *env, jclass){
    return ;//we never release
    if(res != NULL){
        res->is_exit = 1 ;
        if(res->obj!=NULL){
            env->DeleteGlobalRef(res->obj);
            res->obj = NULL;
        }
        sem_destroy(&res->sem_play);
        free(res);
        res = NULL;
    }
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_login
        (JNIEnv *env, jclass, jstring ip){
    if(res==NULL)return false;
    const char* _ip = env-> GetStringUTFChars(ip,NULL);
    hwnet_init(5888);
    int ret = register_nvr(_ip);
    env->ReleaseStringUTFChars(ip,_ip);
    return ret==1?true:false;
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_loginOut
        (JNIEnv *, jclass){
    	if(res==NULL)return false;
    	if(res->user_handle<0)return false;
    	int ret = hwnet_logout(res->user_handle);
        res->user_handle = -1;
        hwnet_release();
    	return ret==1?true:false;
    return false;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_setCallBackObj
        (JNIEnv *env, jclass, jobject obj){
    if(res == NULL) return;
    if (res->obj!=NULL){
        env->DeleteGlobalRef(res->obj);
        res->obj = NULL;
    }
    res->obj = env->NewGlobalRef(obj);
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_readyPlayLive
        (JNIEnv *, jclass){
    if(res == NULL) return false;

    hwplay_init(1,0,0);
    RECT area;
    HW_MEDIAINFO media_head;
    memset(&media_head,0,sizeof(media_head));
    int slot = 0;
    int is_sub = 1;
    int connect_mode = 0;


    media_head.media_fourcc = HW_MEDIA_TAG;
    media_head.au_channel = 1;
    media_head.au_sample = 8;
    media_head.au_bits = 16;
    media_head.adec_code = ADEC_AAC;
    //	media_head.vdec_code = 0x0f;
    media_head.vdec_code = VDEC_H264;// 0x10;

    /*
    res->live_stream_handle = hwnet_get_live_stream(res->user_handle,slot,is_sub,connect_mode,on_live_stream_fun,0);
    //initHi265Decode();
    int ret2 = hwnet_get_live_stream_head(res->live_stream_handle,(char*)&media_head,1024,&res->media_head_len);
     */


//	LOGE(" code= 0x%x",media_head.vdec_code);


    PLAY_HANDLE  ph = hwplay_open_stream((const char*)&media_head,sizeof(media_head),1024*1024,0,area);
    res->play_handle = ph;
    res->isFirstTime = 1;
    hwplay_open_sound(ph);
    //hwplay_set_max_framenum_in_buf(ph,is_playback?25:5);
    int b = hwplay_register_source_data_callback(ph,on_source_callback,0);
    return res->play_handle>=0?true:false;
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_readyPlayTurnLive
        (JNIEnv *env, jclass, jobject obj,jint isPlayback){
    if(res == NULL) {
        LOGE("res=NULL");
        return false;
    }
    jclass clz = env->GetObjectClass(obj);
    int auChannel = 0;
    int auSample = 0;
    int auBits = 0;
    int auCode = 0;
    int vidioCode = 0;
    jfieldID id = env->GetFieldID(clz,"audioChannels","I");
    auChannel = env->GetIntField(obj,id);
    id = env->GetFieldID(clz,"audioSamples","I");
    auSample = env->GetIntField(obj,id);
    id = env->GetFieldID(clz,"audioBitwidth","I");
    auBits = env->GetIntField(obj,id);
    id = env->GetFieldID(clz,"audioCodec","I");
    auCode = env->GetIntField(obj,id);
    id = env->GetFieldID(clz,"videoCodec","I");
    vidioCode = env->GetIntField(obj,id);


    hwplay_init(1,0,0);
    RECT area;
    HW_MEDIAINFO media_head;
    memset(&media_head,0,sizeof(media_head));


    media_head.media_fourcc = HW_MEDIA_TAG;
    media_head.au_channel = auChannel;
    media_head.au_sample = auSample/1000;
    media_head.au_bits = auBits;

    switch (auCode){
        case 0:
            media_head.adec_code = ADEC_AAC;
            break;
        case 1:
            media_head.adec_code = ADEC_G711U;
            break;
        default:
            media_head.adec_code = ADEC_AAC;
            break;
    }
    //  LOGE("video Code=%d",vidioCode);
    switch (vidioCode){
        case 0:
            media_head.vdec_code = VDEC_H264;//ecam
            break;
        case 1:
            media_head.vdec_code = VDEC_H264_ENCRYPT;//bao
            break;
        case 2:
            media_head.vdec_code = VDEC_HIS_H265;//h265
            break;
        case 3:
            media_head.vdec_code = VDEC_HISH265_ENCRYPT;//h265 encrypt
            break;
    }

//	media_head.adec_code = ADEC_AAC;
//	//	media_head.vdec_code = 0x0f;
//	media_head.vdec_code = 0x10;

    PLAY_HANDLE  ph = hwplay_open_stream((const char*)&media_head,sizeof(media_head),1024*1024,isPlayback,area);
    res->play_handle = ph;
    res->isFirstTime = 1;
    res->firstTimestamp = 0;
    res->timestamp = 0;
    res->stream_len=0;
    hwplay_open_sound(ph);
    //  LOGI("ph=%d",ph);

    hwplay_set_max_framenum_in_buf(ph,isPlayback?5:5);
    int remain = 0;
    int stream_buf_len = 0;
    hwplay_get_stream_buf_len(res->play_handle,&stream_buf_len);
    hwplay_get_stream_buf_remain(res->play_handle,&remain);

    LOGI("remain=%d      stream_buf_len=%d    play_handle=%d\n",remain,stream_buf_len, res->play_handle);
    int b = hwplay_register_source_data_callback(ph,on_source_callback,0);
    return res->play_handle>=0?true:false;
}



JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_readyPlayPlayback
        (JNIEnv *env, jclass cls){
    return Java_com_howell_jni_JniUtil_readyPlayLive(env,cls);
}


void fill_net_time (JNIEnv *env,jobject obj,SYSTEMTIME *time){
    if(time==NULL){LOGE("fill net time time==null error");return;}
    LOGI("fill net time");
    jclass clazz = env->GetObjectClass(obj);
    jfieldID id = env->GetFieldID(clazz,"year","S");
    time->wYear = env->GetShortField(obj,id);
    id =  env->GetFieldID(clazz,"month","S");
    time->wMonth = env->GetShortField(obj,id);
    id = env->GetFieldID(clazz,"dayOfWeek","S");
    time->wDayofWeek = env->GetShortField(obj,id);
    id = env->GetFieldID(clazz,"day","S");
    time->wDay = env->GetShortField(obj,id);
    id = env->GetFieldID(clazz,"hour","S");
    time->wHour = env->GetShortField(obj,id);
    id = env->GetFieldID(clazz,"minute","S");
    time->wMinute = env->GetShortField(obj,id);
    id = env->GetFieldID(clazz,"second","S");
    time->wSecond = env->GetShortField(obj,id);
    id = env->GetFieldID(clazz,"msecond","S");
    time->wMilliseconds = env->GetShortField(obj,id);
    env->DeleteLocalRef(clazz);
    LOGI("fill net time:[%04d-%02d-%02d %02d:%02d:%02d]\n",time->wYear,time->wMonth,time->wDay,time->wHour,time->wMinute,time->wSecond);
}


JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_netSetPlayBackTime
        (JNIEnv *env, jclass, jobject beg, jobject end){
    LOGE("net set play back time");
    if (res == NULL) {LOGE("res==null error");return;}
    LOGI("net st play bake time");
    fill_net_time(env,beg,&res->beg);
    fill_net_time(env,end,&res->end);
}


JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_netReadyPlay
        (JNIEnv *, jclass, jint isCrypto, jint isPlayBack, jint slot, jint is_sub){
    if(res == NULL)return false;
    if (res->user_handle==-1){LOGE("net ready play user handle==-1 error");return false;}
    hwplay_init(1,0,0);
    RECT area;
    HW_MEDIAINFO media_head;
    memset(&media_head,0,sizeof(media_head));
    if (!isPlayBack){
        res->live_stream_handle = hwnet_get_live_stream(res->user_handle,slot,is_sub,0,on_live_stream_fun,0);
        hwnet_get_live_stream_head(res->live_stream_handle,(char*)&media_head,1024,&res->media_head_len);
    }else{
        file_stream_t file_info;
        res->file_stream_handle = hwnet_get_file_stream(res->user_handle,slot,res->beg,res->end,on_file_stream_fun,0,&file_info);
        hwnet_get_file_stream_head(res->file_stream_handle,(char*)&media_head,1024,&res->media_head_len);
    }
    LOGI("net ready play get live stream head vdec_code=");
    LOGE(" code= 0x%x   file_stream_handle=%d\n",media_head.vdec_code,res->file_stream_handle );
    LOGE("beg= [%04d-%02d-%02d %02d:%02d:%02d]\n",res->beg.wYear,res->beg.wMonth,res->beg.wDay,res->beg.wHour,res->beg.wMinute,res->beg.wSecond);
    //ap:0 h264  1 h265  2 h264 Crypto  3 h265 Crypto

    if (isCrypto==1){
        media_head.vdec_code = VDEC_HIS_H265;//VDEC_H264_ENCRYPT;//加密  用于bao VDEC_H264     VDEC_H264_ENCRYPT INZ_200系列
    }else if(isCrypto == 0){
        media_head.vdec_code = VDEC_H264;//未加密 用于 ap
    }else if(isCrypto == 2){
        media_head.vdec_code = VDEC_H264_ENCRYPT;//VDEC_HIS_H265;//h265
    }else if(isCrypto == 3){
        media_head.vdec_code = VDEC_HISH265_ENCRYPT;//h265 加密
    }else {
        media_head.vdec_code = VDEC_H264;//默认 未加密 h264 用于 ap
    }

    PLAY_HANDLE  ph = hwplay_open_stream((char*)&media_head,sizeof(media_head),1024*1024,isPlayBack,area);
    hwplay_open_sound(ph);
    hwplay_register_source_data_callback(ph,on_source_callback,1);//user data==0
    res->play_handle = ph;
    LOGI("ready finish  play_handle=%d",ph);
    return res->play_handle>=0?true:false;
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_isNetReady
        (JNIEnv *, jclass){
    LOGI("is net ready?   play_handle=%d    live_stream_handle=%d  file_stream_handle=%d\n",res->play_handle,res->live_stream_handle,res->file_stream_handle);
    if (res==NULL)return false;
    bool ret = true;
    if (res->play_handle==-1){
        ret = false;
    }
    if (res->live_stream_handle==-1&&res->file_stream_handle==-1){
        ret = false;
    }

    return ret;
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_isNetLogin
        (JNIEnv *, jclass){
    if (res==NULL)return false;
    return res->user_handle>-1?true: false;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_releasePlay
        (JNIEnv *, jclass){
    if (res==NULL)
        return;

    int ret = hwplay_release();
    LOGI("hwplay_release  ret = %d\n",ret);
    res->play_handle = -1;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_playView
        (JNIEnv *, jclass){
    if(res == NULL) return;
    if(res->play_handle<0)return;
    res->is_exit = 0;
    res->is_pause = 0;
    if (g_transMgr!=NULL){
        g_transMgr->transDataLen = 0;
    }
  //  hwplay_clear_stream_buf(res->play_handle);
    LOGI("play view playHandle=%d\n",res->play_handle);
    hwplay_play(res->play_handle);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_pauseAndPlayView
        (JNIEnv *, jclass){
    if(res==NULL)return;
    if(res->is_pause){
        res->is_pause = 0;
        sem_post(&res->sem_play);
    }else{
        res->is_pause = 1;
    }
    hwplay_pause(res->play_handle,res->is_pause);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_isPause
        (JNIEnv *, jclass){
    if (res==NULL) return -1;
    return res->is_pause;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_netStopPlay
        (JNIEnv *, jclass){
    if(res == NULL)return;
    int ret = 0;
    if (res->live_stream_handle!=-1){
        ret = hwnet_close_live_stream(res->live_stream_handle);
        LOGI("close live stream ret=%d   stream handle=%d\n",ret,res->live_stream_handle);
        res->live_stream_handle = -1;
    }

    if (res->file_stream_handle!=-1){
        ret = hwnet_close_file_stream(res->file_stream_handle);
        LOGI("close file stream ret = %d   file_stream_handle=%d\n",ret,res->file_stream_handle);
        res->file_stream_handle = -1;
    }
    res->stream_len = 0;

}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_stopView
        (JNIEnv *, jclass){
    if(res == NULL)return;
    int ret = 0;
    res->firstTimestamp = 0;
    res->timestamp = 0;
    ret = hwplay_stop(res->play_handle);
    hwplay_clear_stream_buf(res->play_handle);
    //hwnet_close_live_stream(res->live_stream_handle);
    LOGI("stop view ret=%d   play_handle=%d\n",ret,res->play_handle);

    res->is_exit = 1;
    res->play_handle = -1;

    //deInit265Decode();
}

JNIEXPORT jlong JNICALL Java_com_howell_jni_JniUtil_getFirstTimeStamp
        (JNIEnv *, jclass){
    if (res==NULL)return 0;
    return res->firstTimestamp;
}

JNIEXPORT jlong JNICALL Java_com_howell_jni_JniUtil_getTimeStamp
        (JNIEnv *, jclass){
    if (res==NULL)return 0;
    return res->timestamp;
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_netPtzMove
        (JNIEnv *, jclass, jint flag){
    if (res == NULL)return false;
    ptz_ctrl_t ctrl;
    memset(&ctrl,0,sizeof(ctrl));
    ctrl.slot = 0;
    ctrl.control = 0;
    ctrl.value = 10;
    switch (flag){
        case 0:
            ctrl.cmd = 5;
            break;
        case 1:
            ctrl.cmd = 8;
            break;
        case 2:
            ctrl.cmd = 2;
            break;
        case 3:
            ctrl.cmd = 4;
            break;
        case 4:
            ctrl.cmd = 6;
            break;
        default:
            break;
    }
    return hwnet_ptz_ctrl(res->user_handle,&ctrl)==1?true: false;
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_netPtzCam
        (JNIEnv *, jclass, jint flag){
    if (res == NULL)return false;
    ptz_ctrl_t ctrl;
    memset(&ctrl,0,sizeof(ctrl));
    ctrl.slot = 0;
    ctrl.control = 1;
    ctrl.value = 10;
    switch (flag){
        case 0:
            ctrl.cmd = 7;//stop
            break;
        case 1:
            ctrl.cmd = 3;//tele
            break;
        case 2:
            ctrl.cmd = 4;//wide
            break;
        case 3:
            ctrl.cmd = 5;//far
            break;
        case 4:
            ctrl.cmd = 6;//near
            break;
        default:
            break;
    }
    return hwnet_ptz_ctrl(res->user_handle,&ctrl)==1?true: false;
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_JniUtil_netPtzIris
        (JNIEnv *, jclass, jint flag){
    if (res == NULL)return false;
    ptz_ctrl_t ctrl;
    memset(&ctrl,0,sizeof(ctrl));
    ctrl.slot = 0;
    ctrl.control = 1;
    ctrl.value = 10;
    switch (flag){
        case 0:
            ctrl.cmd = 2;
            break;
        case 1:
            ctrl.cmd = 1;
            break;
        default:
            break;
    }
    return hwnet_ptz_ctrl(res->user_handle,&ctrl)==1?true: false;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_netGetVideoListCount
        (JNIEnv *env, jclass, jobject beg, jobject end){
    if (res==NULL)return -1;
    if(res->user_handle==-1)return -1;
    SYSTEMTIME beg_time,end_time;
    fill_net_time(env,beg,&beg_time);
    fill_net_time(env,end,&end_time);
    LOGI("total_file_list_count=%d   list handle all=%d\n",res->total_file_list_count,res->file_list_handle_all);
    if (res->total_file_list_count==-1 ||  res->file_list_handle_all==-1) {
        res->file_list_handle_all = hwnet_get_file_list(res->user_handle, 0, beg_time, end_time, 0);
        hwnet_get_file_count(res->file_list_handle_all, &res->total_file_list_count);

    }
    LOGI("play_handle=%d  list handle_all=%d    count=%d\n",res->play_handle, res->file_list_handle_all,res->total_file_list_count);
    return res->total_file_list_count;
}

void netCloseFileListNecessary(){
    if (res==NULL)return;
    if (res->file_list_handle!=-1) {

        hwnet_close_file_list(res->file_list_handle);
        res->file_list_handle = -1;
        res->total_file_list_count = -1;
    }
    if (res->file_list_handle_all!=-1){
        hwnet_close_file_list(res->file_list_handle_all);
        res->file_list_handle_all = -1;
        res->total_file_list_count = -1;
    }
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_netCloseVideoList
        (JNIEnv *, jclass){
    netCloseFileListNecessary();
}


JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_netGetStreamLenSomeTime
        (JNIEnv *, jclass){
    if (res == NULL)return -1;
    int len = res->stream_len;
    res->stream_len=0;
    return len;
}


JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_netGetVideoListPageCount
        (JNIEnv *env, jclass, jobject begObj, jobject endObj, jint pageSize, jint curPageNo){
    if(res==NULL){LOGE("res==null error");return -1;}
    if (res->user_handle==-1){LOGE("play handle==-1 error");return -1;}
    SYSTEMTIME beg,end;
    memset(&beg,0,sizeof(beg));
    memset(&end,0,sizeof(end));
    fill_net_time(env,begObj,&beg);
    fill_net_time(env,endObj,&end);
    Pagination page;
    memset(&page,0,sizeof(page));
    page.page_size = pageSize;
    page.page_no = curPageNo;

    netCloseFileListNecessary();

    res->file_list_handle = hwnet_get_file_list_by_page(res->user_handle,0,1,beg,end,0,1,0,&page);
   // hwnet_get_file_list(res->play_handle,0,beg,end,0);
    res->total_file_list_count = page.page_count;
//    LOGI("page count=%d\n",page.page_count);
    if (res->file_list_handle==-1){LOGE("file_list_handle==-1  get file list by page error");}
    return res->file_list_handle>-1?page.page_count:-1;
}


JNIEXPORT jobjectArray JNICALL Java_com_howell_jni_JniUtil_netGetVideoListAll
        (JNIEnv *env, jclass, jint count){
    if(res == NULL) return NULL;
    if (res->file_list_handle==-1)return NULL;
    SYSTEMTIME beg,end;
    jclass clz              = env->FindClass("com/howellsdk/player/ap/bean/ReplayFile");
    jfieldID _begYearId     = env->GetFieldID(clz,"begYear","S");
    jfieldID _begMounthId   = env->GetFieldID(clz,"begMonth","S");
    jfieldID _begDayId      = env->GetFieldID(clz,"begDay","S");
    jfieldID _begHourId     = env->GetFieldID(clz,"begHour","S");
    jfieldID _begMinId      = env->GetFieldID(clz,"begMin","S");
    jfieldID _begSecId      = env->GetFieldID(clz,"begSec","S");

    jfieldID _endYearId     = env->GetFieldID(clz,"endYear","S");
    jfieldID _endMounthId   = env->GetFieldID(clz,"endMonth","S");
    jfieldID _endDayId      = env->GetFieldID(clz,"endDay","S");
    jfieldID _endHourId     = env->GetFieldID(clz,"endHour","S");
    jfieldID _endMinId      = env->GetFieldID(clz,"endMin","S");
    jfieldID _endSecId      = env->GetFieldID(clz,"endSec","S");
    jmethodID consId        = env->GetMethodID(clz,"<init>","()V");

    jobjectArray arry = env->NewObjectArray(count,clz,NULL);
    jobject obj;
    int type = 0;
    for(int i=0;i<count;i++){
        memset(&beg,0,sizeof(beg));
        memset(&end,0,sizeof(end));
        if (hwnet_get_file_detail(res->file_list_handle,i,&beg,&end,&type)==1){
            obj = env->NewObject(clz,consId);
            env->SetShortField(obj,_begYearId,beg.wYear);
            env->SetShortField(obj,_begMounthId,beg.wMonth);
            env->SetShortField(obj,_begDayId,beg.wDay);
            env->SetShortField(obj,_begHourId,beg.wHour);
            env->SetShortField(obj,_begMinId,beg.wMinute);
            env->SetShortField(obj,_begSecId,beg.wSecond);
            env->SetShortField(obj,_endYearId,end.wYear);
            env->SetShortField(obj,_endMounthId,end.wMonth);
            env->SetShortField(obj,_endDayId,end.wDay);
            env->SetShortField(obj,_endHourId,end.wHour);
            env->SetShortField(obj,_endMinId,end.wMinute);
            env->SetShortField(obj,_endSecId,end.wSecond);

            env->SetObjectArrayElement(arry,i,obj);
        }else {
            break;
        }
    }
    return arry;
}

JNIEXPORT jobjectArray JNICALL Java_com_howell_jni_JniUtil_netGetVideoList
        (JNIEnv *env, jclass, jint startCount, jint endCount){
    if(res == NULL) return NULL;
    if (res->file_list_handle_all==-1)return NULL;
    SYSTEMTIME beg,end;
    jclass clz              = env->FindClass("com/howellsdk/player/ap/bean/ReplayFile");
    jfieldID _begYearId     = env->GetFieldID(clz,"begYear","S");
    jfieldID _begMounthId   = env->GetFieldID(clz,"begMonth","S");
    jfieldID _begDayId      = env->GetFieldID(clz,"begDay","S");
    jfieldID _begHourId     = env->GetFieldID(clz,"begHour","S");
    jfieldID _begMinId      = env->GetFieldID(clz,"begMin","S");
    jfieldID _begSecId      = env->GetFieldID(clz,"begSec","S");

    jfieldID _endYearId     = env->GetFieldID(clz,"endYear","S");
    jfieldID _endMounthId   = env->GetFieldID(clz,"endMonth","S");
    jfieldID _endDayId      = env->GetFieldID(clz,"endDay","S");
    jfieldID _endHourId     = env->GetFieldID(clz,"endHour","S");
    jfieldID _endMinId      = env->GetFieldID(clz,"endMin","S");
    jfieldID _endSecId      = env->GetFieldID(clz,"endSec","S");
    jmethodID consId        = env->GetMethodID(clz,"<init>","()V");
    if (endCount==startCount)endCount+=1;
    int count = endCount - startCount;
    if (count<0)count = -count;
    jobjectArray arry = env->NewObjectArray(count,clz,NULL);
    jobject obj;
    int type = 0;
//    LOGI("startCount=%d  endCount=%d\n",startCount,endCount);
    if(endCount>startCount) {
        for (int i = startCount,index=0; i < endCount; i++,index++) {
            memset(&beg, 0, sizeof(beg));
            memset(&end, 0, sizeof(end));
            if (hwnet_get_file_detail(res->file_list_handle_all, i, &beg, &end, &type) == 1) {
                obj = env->NewObject(clz, consId);
                env->SetShortField(obj, _begYearId, beg.wYear);
                env->SetShortField(obj, _begMounthId, beg.wMonth);
                env->SetShortField(obj, _begDayId, beg.wDay);
                env->SetShortField(obj, _begHourId, beg.wHour);
                env->SetShortField(obj, _begMinId, beg.wMinute);
                env->SetShortField(obj, _begSecId, beg.wSecond);
                env->SetShortField(obj, _endYearId, end.wYear);
                env->SetShortField(obj, _endMounthId, end.wMonth);
                env->SetShortField(obj, _endDayId, end.wDay);
                env->SetShortField(obj, _endHourId, end.wHour);
                env->SetShortField(obj, _endMinId, end.wMinute);
                env->SetShortField(obj, _endSecId, end.wSecond);

                env->SetObjectArrayElement(arry, index, obj);
            } else {
                LOGE("hwnet_get_file_detail error\n");
                break;
            }
        }
    }else{
//        int index=0;
        for (int j = startCount,index=0; j > endCount; j--,index++) {

            memset(&beg, 0, sizeof(beg));
            memset(&end, 0, sizeof(end));
            if (hwnet_get_file_detail(res->file_list_handle_all, j, &beg, &end, &type) == 1) {
//                LOGI("index=%d  j=%d  M=%d-D=%d H=%d:m=%d\n",index,j,beg.wMonth,beg.wDay,beg.wHour,beg.wMinute);

                obj = env->NewObject(clz, consId);
                env->SetShortField(obj, _begYearId, beg.wYear);
                env->SetShortField(obj, _begMounthId, beg.wMonth);
                env->SetShortField(obj, _begDayId, beg.wDay);
                env->SetShortField(obj, _begHourId, beg.wHour);
                env->SetShortField(obj, _begMinId, beg.wMinute);
                env->SetShortField(obj, _begSecId, beg.wSecond);
                env->SetShortField(obj, _endYearId, end.wYear);
                env->SetShortField(obj, _endMounthId, end.wMonth);
                env->SetShortField(obj, _endDayId, end.wDay);
                env->SetShortField(obj, _endHourId, end.wHour);
                env->SetShortField(obj, _endMinId, end.wMinute);
                env->SetShortField(obj, _endSecId, end.wSecond);
                env->SetObjectArrayElement(arry, index, obj);
//                index++;
            } else {
                LOGE("hwnet_get_file_detail error\n");
                break;
            }
        }
    }
    return arry;
}


///////////////////////////////
/////////////////////////////////////
///////////////////////////////////////


/**
 * audio
 */




JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeAudioInit
        (JNIEnv *env, jclass){
    if(self==NULL){
        self = (TAudioPlay*)malloc(sizeof(TAudioPlay));
        memset(self,0,sizeof(TAudioPlay));
        env->GetJavaVM(&self->jvm);
        self->jsonMethod_ready=0;
        self->method_ready = 0;
        self->stop = 0;
    }
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeAudioDeinit
        (JNIEnv *env, jclass){
    return;//we never release
    if(self!=NULL){
        if(self->obj!=NULL){
            env->DeleteGlobalRef(self->obj);
            self->obj = NULL;
        }
        env->DeleteGlobalRef( self->data_array);
        free(self);
        self = NULL;
    }
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeAudioSetCallbackObject
        (JNIEnv *env, jclass, jobject obj, jint flag){
    if(self==NULL)return;
    switch(flag){
        case 0:
            self->obj = env->NewGlobalRef(obj);
            break;
        default:
            break;
    }
}


JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeAudioSetCallbackMethodName
        (JNIEnv *env, jclass, jstring str, jint flag){
    if(self==NULL)return;
    if(self->obj==NULL)return;
    jclass clz = env->GetObjectClass(self->obj);
    char *_str = (char *)env->GetStringUTFChars(str,NULL);
    switch(flag){
        case 0:{
            self->data_length_id = env->GetFieldID(clz, _str, "I");
        }
            break;
        case 1:{
            jfieldID id = env->GetFieldID(clz,_str,"[B");
            jbyteArray data = (jbyteArray)env->GetObjectField(self->obj,id);
            self->data_array = (jbyteArray)env->NewGlobalRef(data);
            env->DeleteLocalRef(data);
            self->data_array_len =env->GetArrayLength(self->data_array);
        }
            break;
        default:
            break;
    }
    env->ReleaseStringUTFChars(str,_str);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeAudioStop
        (JNIEnv *, jclass){
    if(self==NULL)return;
    self->stop=1;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeAudioBPlayable
        (JNIEnv *, jclass){
    if(self==NULL)return;
    self->stop=0;
}




/**
 * transmission
 */




int on_my_connect(const char* session_id){
    LOGI("on my connect");
    if(g_transMgr==NULL){LOGE("on my connect g_transMgr==null we return");return -1;}

    //		if(g_transMgr->jvm->AttachCurrentThread( &g_transMgr->env, NULL) != JNI_OK) {
    //				LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
    //				return -1;
    //		}

    JNIEnv *env = NULL;
    //		if(g_transMgr->jvm->GetEnv((void **)&env,JNI_VERSION_1_4)!=JNI_OK){
    //			LOGE("get env error");
    //			return -1;
    //		}

    JavaVM * _jvm = g_transMgr->jvm;


    if(_jvm->AttachCurrentThread( &env, NULL) != JNI_OK) {
        LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
        return 0;
    }



    jstring str = env->NewStringUTF(session_id);
    env->CallVoidMethod(g_transMgr->callback_obj,g_transMgr->on_connect_method,str);

    env->DeleteLocalRef(str);
    if (_jvm->DetachCurrentThread() != JNI_OK) {
        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
    }


    return 0;
}
int on_record_list_res(JNIEnv *env,const char* jsonStr,int len){
    if(jsonStr==NULL)return -1;
    if(g_transMgr==NULL)return -1;
    if(g_transMgr->callback_obj==NULL)return -1;
    if(g_transMgr->on_recordFile_method==NULL)return -1;
/*	JNIEnv *env = NULL;
	JavaVM * _jvm = g_transMgr->jvm;
	if(_jvm->AttachCurrentThread( &env, NULL) != JNI_OK) {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
		return 0;
	}
	*/
    jstring str = env->NewStringUTF(jsonStr);
    env->CallVoidMethod(g_transMgr->callback_obj,g_transMgr->on_recordFile_method,str);
    env->DeleteLocalRef(str);
    /*
    if (_jvm->DetachCurrentThread() != JNI_OK) {
        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
    }
    */
    return 0;
}

//FIXME 只有 SubscribeAck  GetCamreaAck  GetRecordedFilesAck  PtzCtrlAck
int on_ack_all_res(JNIEnv *env,int cmd,const char* jsonStr,int len){
    if(jsonStr==NULL)return -1;
    if(g_transMgr==NULL)return -1;
    if(g_transMgr->callback_obj==NULL)return -1;
    if(g_transMgr->on_ack_all_method==NULL)return -1;
    jstring str = env->NewStringUTF(jsonStr);
    env->CallVoidMethod(g_transMgr->callback_obj,g_transMgr->on_ack_all_method,cmd,str);
    LOGE("CallVoidMethod  on_ack_all_method\n");
    env->DeleteLocalRef(str);
    return 0;
}


int on_my_ack_res(int msgCommand,void * res,int len){
    LOGE("on my ack res msgCommand=0x%x\n",msgCommand);
    JNIEnv *env = NULL;
    JavaVM * _jvm = g_transMgr->jvm;
    if (g_transMgr==NULL){LOGE("g_transMGR==null return");return 0;}
    if(g_transMgr->callback_obj==NULL){LOGE("callobj==null return");return 0;}
//    env = g_transMgr->env;
    LOGE("cmd=0x%x\n",msgCommand);
//    if (msgCommand==0x13){//FIXME 0x13 socket disconnect 分主动 被动，主动为上层 deinit时 自上层线程中
//        if (g_transMgr->callback_obj!=NULL&&g_transMgr->on_disconnect_method!=NULL) {
//            LOGE("call on_disconnect_method method");
//            env->CallVoidMethod(g_transMgr->callback_obj, g_transMgr->on_disconnect_method);
//            LOGE("call ok");
//        }
//        return 0;
//    }

    if(_jvm->AttachCurrentThread( &env, NULL) != JNI_OK) {
        LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
        return 0;
    }

    switch (msgCommand) {

#if 1
        case 0x13://websocket disconnect
        {
            if (g_transMgr->callback_obj!=NULL&&g_transMgr->on_disconnect_method!=NULL) {
                LOGE("call on_disconnect_method method");
                env->CallVoidMethod(g_transMgr->callback_obj, g_transMgr->on_disconnect_method);
                LOGE("call ok");
            }
        }
            break;
#endif
        case 0x15:
        {
            //JNIEnv *env = NULL;
            //JavaVM * _jvm = g_transMgr->jvm;
            //if(_jvm->AttachCurrentThread( &env, NULL) != JNI_OK) {
            //	LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
            //	return 0;
            //}
            if (g_transMgr->on_subscribe_method==NULL||g_transMgr->callback_obj==NULL)break;
            jstring  jsonStr = env->NewStringUTF((char *)res);
            env->CallVoidMethod(g_transMgr->callback_obj,g_transMgr->on_subscribe_method,jsonStr);
            env->DeleteLocalRef(jsonStr);
            /*if (_jvm->DetachCurrentThread() != JNI_OK) {
                LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
            }*/
        }
            break;
        case 0x17:
        {
            //kCmdUnsubscribeAck;
            LOGI("UnsubscribeAck");
            if (g_transMgr->on_unsubscribe_method==NULL||g_transMgr->callback_obj==NULL)break;
            jstring  jsonStr = env->NewStringUTF((char *)res);
            env->CallVoidMethod(g_transMgr->callback_obj,g_transMgr->on_unsubscribe_method,jsonStr);
            env->DeleteLocalRef(jsonStr);
        }
            break;
        case 0x104:
        {
            LOGI("cmd 104   %s",res);
            on_record_list_res(env,(const char*)res,len);
        }
            break;
        case 0x106:{//ptz
//            jstring  jsonStr = env->NewStringUTF((char *)res);
            LOGI("jsonStr= %s\n",res);
//            env->DeleteLocalRef(jsonStr);
        }
            break;
        default:
            break;
    }
    //on_ack_all_res(env,msgCommand,(const char*)res,len);
    if(_jvm==NULL){
        LOGE("error  _jvm==null   line=%d",__LINE__);
    }


    if (_jvm->DetachCurrentThread() != JNI_OK) {
        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
    }
    LOGE("call msg ok");
    return 0;
}

long long g_timeStamp = 0;

long getTimeStamp(){

    g_timeStamp += 40;
    return g_timeStamp;
}

int on_my_socket_error_fun(int flag){//0 socket 1 sync  2 packet receive false   3  http !=200
    LOGE("on my socket_error fun  flag=%d\n",flag);
    if (g_transMgr == NULL) return -1;
    if (g_transMgr->callback_obj==NULL || g_transMgr->on_socket_error_method == NULL)return -1;
    JNIEnv *env = g_transMgr->env;
    JavaVM * _jvm = g_transMgr->jvm;
    if (flag==0){
        if (env!=NULL) {
            env->CallVoidMethod(g_transMgr->callback_obj, g_transMgr->on_socket_error_method, flag);
        }
        return 0;
    }

    if(_jvm->AttachCurrentThread( &env, NULL) != JNI_OK) {
        LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
        return 0;
    }
    LOGE("call void method flag=%d\n",flag);
    if (env!=NULL) {
        env->CallVoidMethod(g_transMgr->callback_obj, g_transMgr->on_socket_error_method, flag);
    }
    LOGE("call void method ok");
    if (_jvm->DetachCurrentThread() != JNI_OK) {
        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
    }

    return 0;
}


int on_my_data_fun(int type,const char *data,int len){
  //  LOGI("  on data fun  len=%d\n",len);
    if(res == NULL){
        LOGE("res == null");
        return -1;
    }
    if(res->is_exit == 1){
        LOGE("res is exit");
        return -1;
    }
    if(res->play_handle==-1){
        LOGE("res play heandle = -1");
        return -1;
    }

#if 0
    stream_head head ;
	memset(&head,0,sizeof(head));
	head.len = len + sizeof(stream_head);
	head.sys_time = time(NULL);
	head.tag = 0x48574D49;
	//	head.time_stamp =  (unsigned long long)0 / 90 * 1000;//FIXME
	//	if(media_type == kFrameTypeAudio){
	//		head.time_stamp =  (unsigned long long)timestamp / 8 * 1000;
	//	}

	if(type==1){
		head.type =1;
		head.time_stamp = getTimeStamp();
	}else if(type == 4){
		head.type = 2;
	}else{
		head.type = 0;
	}
#endif

    if (res->is_pause){
        sem_wait(&res->sem_play);
    }


    if(g_transMgr!=NULL){
        g_transMgr->transDataLen += len;
    }
//    pthread_mutex_lock(&res->lock_play);

    int num = 0;
    int ret = 0;

    int remain = 0;
    int stream_buf_len = 0;
    hwplay_get_stream_buf_len(res->play_handle,&stream_buf_len);

    do{
        hwplay_get_stream_buf_remain(res->play_handle,&remain);
//        LOGI("remain=%d  len=%d\n",remain,len);
        if(remain>len){
            break;
        }
        usleep(40000);
        g_num++;
    }while(g_num<50);
    hwplay_get_stream_buf_remain(res->play_handle,&remain);
//    LOGI("before   remain=%d  len=%d\n",remain,len);
    ret = hwplay_input_data(res->play_handle, data ,len);
    hwplay_get_stream_buf_remain(res->play_handle,&remain);
//    LOGI("after   ret=%d  remain=%d  len=%d\n",ret,remain,len);
//    pthread_mutex_unlock(&res->lock_play);
    g_num = 0;
    return 0;

    //Fixme
}







JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transInit
        (JNIEnv *env, jclass){
    if(g_transMgr==NULL){
        g_transMgr = (TRANS_T*)malloc(sizeof(TRANS_T));
        memset(g_transMgr,0,sizeof(TRANS_T));
        env->GetJavaVM(&g_transMgr->jvm);
    }
    trans_init(on_my_connect,on_my_ack_res,on_my_data_fun,on_my_socket_error_fun);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transDeinit
        (JNIEnv *env, jclass){
    trans_deInit();
    return;//we never release
    if(g_transMgr!=NULL){
        if(g_transMgr->callback_obj!=NULL){
            env->DeleteGlobalRef(g_transMgr->callback_obj);
            g_transMgr->callback_obj = NULL;
        }
        free(g_transMgr);
        g_transMgr = NULL;
    }
}

JNIEXPORT int JNICALL Java_com_howell_jni_JniUtil_transConnect
        (JNIEnv *env, jclass clazz, jstring ip, jint port, jboolean isSSL, jint type, jstring id, jstring name, jstring pwd){
    if(g_transMgr==NULL)return -1;

    trans_set_no_use_ssl(!isSSL);

    const char * _ip = env->GetStringUTFChars(ip,0);
    const char * _id = env->GetStringUTFChars(id,0);
    const char * _name = env->GetStringUTFChars(name,0);
    const char * _pwd = env->GetStringUTFChars(pwd,0);
    LOGI("JniUtil_transConnect");
    g_transMgr->env = env;
    int ret = trans_connect(type,_id,_name,_pwd,_ip,port);
    LOGI("ret=%d\n",ret);
    env->ReleaseStringUTFChars(id,_id);
    env->ReleaseStringUTFChars(name,_name);
    env->ReleaseStringUTFChars(pwd,_pwd);
    env->ReleaseStringUTFChars(ip,_ip);
    LOGI("transConnect over");
    return ret;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transDisconnect
        (JNIEnv *, jclass){
    trans_disconnect();
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transSetCallBackObj
        (JNIEnv *env, jclass, jobject obj, jint flag){
    if(g_transMgr==NULL)return;
    if (obj==NULL){
        if ( g_transMgr->callback_obj!=NULL){
            env->DeleteGlobalRef(g_transMgr->callback_obj);
            g_transMgr->callback_obj=NULL;
            return;
        }
    }
    switch(flag){
        case 0:
            LOGE("set callback obj  new");
            g_transMgr->callback_obj = env->NewGlobalRef(obj);
            break;
        default:
            break;
    }

}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transSetCallbackMethodName
        (JNIEnv *env, jclass, jstring method, jint flag){
    if(g_transMgr==NULL) return;
    const char * _mehtod = env->GetStringUTFChars(method,0);
    switch(flag){
        case 0:{
            jclass clz = env->GetObjectClass(g_transMgr->callback_obj);
            g_transMgr->on_connect_method = env->GetMethodID(clz,_mehtod,"(Ljava/lang/String;)V");
            break;
        }

        case 1:{
            jclass clz = env->GetObjectClass(g_transMgr->callback_obj);
            g_transMgr->on_disconnect_method = env->GetMethodID(clz,_mehtod,"()V");
            break;
        }

        case 2:{
            jclass clz = env->GetObjectClass(g_transMgr->callback_obj);
            g_transMgr->on_recordFile_method = env->GetMethodID(clz,_mehtod,"(Ljava/lang/String;)V");
            break;
        }

        case 3:{
            jclass clz = env->GetObjectClass(g_transMgr->callback_obj);
            g_transMgr->on_socket_error_method = env->GetMethodID(clz,_mehtod,"(I)V");
            break;
        }
        case 4:{
            jclass clz = env->GetObjectClass(g_transMgr->callback_obj);
            g_transMgr->on_subscribe_method = env->GetMethodID(clz,_mehtod,"(Ljava/lang/String;)V");
            break;
        }
        case 5:{
            jclass clz = env->GetObjectClass(g_transMgr->callback_obj);
            g_transMgr->on_ack_all_method = env->GetMethodID(clz,_mehtod,"(ILjava/lang/String;)V");
            break;
        }
        case 6:{
            jclass clz = env->GetObjectClass(g_transMgr->callback_obj);
            g_transMgr->on_unsubscribe_method = env->GetMethodID(clz,_mehtod,"(Ljava/lang/String;)V");
            break;
        }

        default:
            break;
    }
    env->ReleaseStringUTFChars(method,_mehtod);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transSubscribe
        (JNIEnv *env, jclass, jstring jsonStr, jint jsonLen){
    const char *_jsonStr = env->GetStringUTFChars(jsonStr,0);
    trans_subscribe(_jsonStr,jsonLen);
    env->ReleaseStringUTFChars(jsonStr,_jsonStr);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transUnsubscribe
        (JNIEnv *env, jclass){
    trans_unsubscribe();
}


JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_catchPic
        (JNIEnv *env, jclass, jstring path){
    if(res==NULL)return;
    if(res->play_handle==-1)return;
    const char *_path = env->GetStringUTFChars(path,0);
    //int ret = hwplay_save_to_jpg(res[index]->play_handle,temp,70);

    hwplay_save_to_jpg(res->play_handle,_path,70);

    env->ReleaseStringUTFChars(path,_path);

}



JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_transGetStreamLenSomeTime
        (JNIEnv *, jclass){
    if(g_transMgr==NULL)return 0;
    int streamLen = g_transMgr->transDataLen;
    g_transMgr->transDataLen = 0;
    return streamLen;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transGetCam
        (JNIEnv *env, jclass, jstring jsonStr, jint len){
    const char *_jsonStr = env->GetStringUTFChars(jsonStr,0);
    trans_getCamrea(_jsonStr,len);
    env->ReleaseStringUTFChars(jsonStr,_jsonStr);

}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transGetRecordFiles
        (JNIEnv *env, jclass, jstring jsonStr, jint len){
    const char *_jsonStr = env->GetStringUTFChars(jsonStr,0);
    trans_getRecordFiles(_jsonStr,len);
    env->ReleaseStringUTFChars(jsonStr,_jsonStr);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transSetCrt
        (JNIEnv *env, jclass, jstring ca, jstring client, jstring key){
    const char * _ca = env->GetStringUTFChars(ca,0);
    const char * _client = env->GetStringUTFChars(client,0);
    const char * _key  = env->GetStringUTFChars(key,0);

    trans_set_crt(_ca,_client,_key);

    env->ReleaseStringUTFChars(ca,_ca);
    env->ReleaseStringUTFChars(client,_client);
    env->ReleaseStringUTFChars(key,_key);


}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transSetCrtPaht
        (JNIEnv *env, jclass, jstring caPath, jstring clientPath, jstring keyPath){
    const char *_caPath = env->GetStringUTFChars(caPath,0);
    const char * _clientPath = env->GetStringUTFChars(clientPath,0);
    const char * _keyPath = env->GetStringUTFChars(keyPath,0);


    trans_set_crt_path(_caPath,_clientPath,_keyPath);

    env->ReleaseStringUTFChars(caPath,_caPath);
    env->ReleaseStringUTFChars(clientPath,_clientPath);
    env->ReleaseStringUTFChars(keyPath,_keyPath);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_transPTZControl
        (JNIEnv *env, jclass, jstring jsonStr, jint jsonLen){
    const char *_jsonStr = env->GetStringUTFChars(jsonStr,0);
    trans_Ptz(_jsonStr,jsonLen);
    env->ReleaseStringUTFChars(jsonStr,_jsonStr);
}



JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_turnInputViewData
        (JNIEnv *env, jclass, jbyteArray byteArray, jint len){
    if(res == NULL){
        LOGE("res == null");
        return ;
    }
    if(res->is_exit == 1){
        LOGE("res is exit");
        return ;
    }
    if(res->play_handle==-1){
        LOGE("res play heandle = -1");
        return ;
    }

    jbyte * data ;
    data = env->GetByteArrayElements(byteArray,0);

    if(g_transMgr!=NULL){
        g_transMgr->transDataLen += len;
    }
    int ret = hwplay_input_data(res->play_handle,(const char*) data ,len);
    env->ReleaseByteArrayElements(byteArray,data,0);
}


//////////////////////////////////////////////
// ecam
/////////////////////////////////////////

typedef struct{
    ecam_stream_req_t* req;
    struct ecam_stream_req_context * context;
    jobject callbackObj;
    int isBack;
    int ecamDataLen;

}Ecam_T;

static Ecam_T* g_ecamMgr = NULL;
static pthread_once_t once_ctrl = PTHREAD_ONCE_INIT;

static void global_init(void)
{
    ice_global_init();
}

void onStreamArrive(ecam_stream_req_t * req,ECAM_STREAM_REQ_FRAME_TYPE media_type,const char * data, size_t len, uint32_t timestamp){

    if (res==NULL|| g_ecamMgr==NULL)
        return;
    if(res->play_handle==-1){
        LOGE("onStreamArrive play_handle == -1");
        return;
    }

    //FIXME add pause
    if(res->is_pause){
        sem_wait(&res->sem_play);
    }




    g_ecamMgr->ecamDataLen += len;

    stream_head head ;
    head.len = len + sizeof(stream_head);
    head.sys_time = time(NULL);
    head.tag = 0x48574D49;
    head.time_stamp =  (unsigned long long)timestamp / 90 * 1000;
    if(media_type == kFrameTypeAudio){
        head.time_stamp =  (unsigned long long)timestamp / 8 * 1000;
    }
    head.type = media_type;
    int ret = 0;
    if (g_ecamMgr->isBack==0){
        ret = hwplay_input_data(res->play_handle,(char*)&head, sizeof(head));
        if (ret!=1){
            LOGE("onStreamArrive play live  input data error\n");
            return;
        }
        hwplay_input_data(res->play_handle, data ,len);
    }else{

        ret = hwplay_input_data(res->play_handle,(char*)&head, sizeof(head));
        if (ret!=1){
            LOGE("onStreamArrive play back  input data error\n");
            return;
        }
        hwplay_input_data(res->play_handle,data,len);

//        while (true){//暂停后 buffer 满了 stream come input error
//            if (!hwplay_input_data(res->play_handle,(char*)&head, sizeof(head))){
//                usleep(10000);
//                LOGE("onStreamArrive  play back input data error\n");
//                continue;
//            }
//            if (!hwplay_input_data(res->play_handle,data,len)){
//                usleep(10000);
//                LOGE("onStreamArrive  play back input data error\n");
//                continue;
//            }
//        }

    }
}


void fill_context(JNIEnv * env,jobject obj, struct ecam_stream_req_context* c) {
    if(env==NULL|| obj==NULL || c==NULL)return;
    jclass clazz = env->GetObjectClass(obj);
    jfieldID id = env->GetFieldID(clazz,"playback","I");
    jint _playback = env->GetIntField(obj,id);

    id = env->GetFieldID(clazz,"beg","J");
    jlong _beg = env->GetLongField(obj,id);

    id = env->GetFieldID(clazz,"end","J");
    jlong _end = env->GetLongField(obj,id);

    id = env->GetFieldID(clazz,"re_invite","I");
    jint _re_invite = env->GetIntField(obj,id);

    id = env->GetFieldID(clazz,"method_bitmap","I");
    jint _bitmapID = env->GetIntField(obj,id);

    id = env->GetFieldID(clazz,"udp_addr", "Ljava/lang/String;");
    jstring _udp_addr = (jstring) env->GetObjectField(obj, id);

    id = env->GetFieldID(clazz,"udp_port","I");
    jint _udp_port = env->GetIntField(obj,id);

    id = env->GetFieldID(clazz,"ice_opt","Lcom/howellsdk/player/ecam/bean/StreamReqIceOpt;");
    jobject _iceObj = env->GetObjectField(obj,id);

    id = env->GetFieldID(clazz,"crypto","Lcom/howellsdk/player/ecam/bean/Crypto;");
    jobject _cryptoObj = env->GetObjectField(obj,id);

    id = env->GetFieldID(clazz,"channel","I");
    jint _channel = env->GetIntField(obj,id);

    id = env->GetFieldID(clazz,"stream","I");
    jint _stream= env->GetIntField(obj,id);

    env->DeleteLocalRef(clazz);
    ///////////////////// ice obj ////////////////////
    clazz = env->GetObjectClass(_iceObj);
    id = env->GetFieldID(clazz,"comp_cnt","I");
    jint _comp_cnt = env->GetIntField(_iceObj,id);

    id = env->GetFieldID(clazz,"stun_addr","Ljava/lang/String;");
    jstring  _stun_addr= (jstring) env->GetObjectField(_iceObj, id);

    id = env->GetFieldID(clazz,"stun_port","I");
    jint _stun_port=env->GetIntField(_iceObj,id);

    id = env->GetFieldID(clazz,"turn_addr","Ljava/lang/String;");
    jstring _turn_addr = (jstring) env->GetObjectField(_iceObj, id);

    id = env->GetFieldID(clazz,"turn_port","I");
    jint _turn_port = env->GetIntField(_iceObj,id);

    id = env->GetFieldID(clazz,"turn_tcp","I");
    jint _turn_tcp = env->GetIntField(_iceObj,id);

    id = env->GetFieldID(clazz,"turn_username","Ljava/lang/String;");
    jstring  _turn_username = (jstring) env->GetObjectField(_iceObj, id);

    id = env->GetFieldID(clazz,"turn_password","Ljava/lang/String;");
    jstring _turn_password = (jstring) env->GetObjectField(_iceObj, id);
    env->DeleteLocalRef(clazz);
    ////////////////crypto  obj ////////////////////////
    clazz = env->GetObjectClass(_cryptoObj);
    id = env->GetFieldID(clazz,"enable","I");
    jint _enable=env->GetIntField(_cryptoObj,id);
    env->DeleteLocalRef(clazz);

    const char * c_udp_addr = env->GetStringUTFChars(_udp_addr,0);
    const char * c_trun_addr = env->GetStringUTFChars(_turn_addr,0);
    const char * c_stun_addr = env->GetStringUTFChars(_stun_addr,0);
    const char * c_turn_name = env->GetStringUTFChars(_turn_username,0);
    const char * c_turn_pwd = env->GetStringUTFChars(_turn_password,0);
    g_ecamMgr->isBack       = _playback;
    c->playback             = _playback;
    LOGI("123","c->playback=%d",c->playback);
    c->beg                  = _beg;
    c->end                  = _end;
    c->re_invite            = _re_invite;
    c->method_map           = _bitmapID;
    c->udp_port             = _udp_port;
    c->channel              = _channel;
    c->stream               = _stream;
    c->crypto.enable        = _enable;
    c->ice_opt.comp_cnt     = _comp_cnt;
    c->ice_opt.stun_port    = _stun_port;
    c->ice_opt.turn_port    = _turn_port;
    c->ice_opt.turn_tcp     = _turn_tcp;
    strcpy(c->udp_addr,c_udp_addr);
    strcpy(c->ice_opt.stun_addr,c_stun_addr);
    strcpy(c->ice_opt.turn_addr,c_trun_addr);
    strcpy(c->ice_opt.turn_username,c_turn_name);
    strcpy(c->ice_opt.turn_password,c_turn_pwd);

    env->ReleaseStringUTFChars(_udp_addr,c_udp_addr);
    env->ReleaseStringUTFChars(_stun_addr,c_stun_addr);
    env->ReleaseStringUTFChars(_turn_addr,c_trun_addr);
    env->ReleaseStringUTFChars(_turn_username,c_turn_name);
    env->ReleaseStringUTFChars(_turn_password,c_turn_pwd);
}


JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_ecamInit
        (JNIEnv *env, jclass,jstring account){
    pthread_once(&once_ctrl,global_init);
    if (g_ecamMgr==NULL){
        g_ecamMgr = (Ecam_T *) malloc(sizeof(Ecam_T));
        memset(g_ecamMgr,0,sizeof(Ecam_T));

        g_ecamMgr->context = (ecam_stream_req_context *) malloc(sizeof(struct ecam_stream_req_context));
        memset(g_ecamMgr->context,0,sizeof(struct ecam_stream_req_context));
        const char * _account= env->GetStringUTFChars(account,NULL);
        g_ecamMgr->req = ecam_stream_req_new(_account);
        env->ReleaseStringUTFChars(account,_account);
        ecam_stream_req_regist_stream_cb(g_ecamMgr->req,onStreamArrive);
    }

}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_ecamDeinit
        (JNIEnv *env, jclass){
    if (g_ecamMgr==NULL)return;
    ecam_stream_req_free(g_ecamMgr->req);
    if (g_ecamMgr->context!=NULL){
        free(g_ecamMgr->context);
        g_ecamMgr->context = NULL;
    }
    if (g_ecamMgr->callbackObj!=NULL){
        env->DeleteGlobalRef(g_ecamMgr->callbackObj);
        g_ecamMgr->callbackObj = NULL;
    }
    free(g_ecamMgr);
    g_ecamMgr = NULL;
}


JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_ecamSetCallbackObj
        (JNIEnv *env, jclass, jobject obj, jint flag){
    if (g_ecamMgr==NULL)return;
    switch (flag){
        case 0:
            if (g_ecamMgr->callbackObj==NULL){
                g_ecamMgr->callbackObj = env->NewGlobalRef(obj);
            }
            break;
        default:
            break;
    }
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_ecamSetContextObj
        (JNIEnv *env, jclass, jobject obj){
    if (g_ecamMgr==NULL||obj==NULL){
        LOGE("g_ecamMgr  g_ecamMgr==null  obj==null");
        return;
    }
    if (g_ecamMgr->context==NULL){
        LOGE("context == NULL");
        return;
    }
    fill_context(env,obj,g_ecamMgr->context);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_ecamGetAudioType
        (JNIEnv *, jclass){
    if (g_ecamMgr==NULL){LOGE("g_ecamMgr==null");return -1;}
    char *desc = (char *) malloc(100);
    memset(desc,0,100);
    int payload = 0;
    int ret = ecam_stream_req_get_audio(g_ecamMgr->req,desc,&payload);
    int audio_type = -1;
    if (ret==1){
        if(strstr(desc,"pcmu") != NULL || strstr(desc,"PCMU") != NULL){
            audio_type = 1;
        }else{
            audio_type = 0;
        }
    }else{
        LOGE("ecam_stream_req_get audio error ret=%d\n",ret);
    }
    free(desc);
    return audio_type;
}

JNIEXPORT jstring JNICALL Java_com_howell_jni_JniUtil_ecamPrepareSDP
        (JNIEnv *env, jclass){
    if (g_ecamMgr==NULL||g_ecamMgr->context==NULL) return NULL;
    char *local_sdp = ecam_stream_req_prepare_sdp(g_ecamMgr->req,g_ecamMgr->context);
    return env->NewStringUTF((const char *)local_sdp);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_ecamHandleRemoteSDP
        (JNIEnv *env, jclass,  jstring dialogID, jstring remoteSDP){
    if (dialogID==NULL||remoteSDP==NULL||g_ecamMgr==NULL||g_ecamMgr->context==NULL)return;
    const char * _dialogID = env->GetStringUTFChars(dialogID,0);
    const char * _remoteSDP= env->GetStringUTFChars(remoteSDP,0);
    ecam_stream_req_handle_remote_sdp(g_ecamMgr->req,g_ecamMgr->context,_dialogID,_remoteSDP);
    env->ReleaseStringUTFChars(dialogID,_dialogID);
    env->ReleaseStringUTFChars(remoteSDP,_remoteSDP);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_ecamStart
        (JNIEnv *, jclass){
    if (g_ecamMgr==NULL) {
        LOGE("g_ecamMgr==null");
        return -1;
    }
    LOGI("isback=%d  context isBack=%d  ",g_ecamMgr->isBack,g_ecamMgr->context->playback);



    return ecam_stream_req_start(g_ecamMgr->req,g_ecamMgr->context,5000);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_ecamStop
        (JNIEnv *, jclass){
    if (g_ecamMgr==NULL) return -1;
    g_ecamMgr->ecamDataLen = 0;
    LOGI("ecam_stream stop");
    return ecam_stream_req_stop(g_ecamMgr->req,3000);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_ecamGetMethod
        (JNIEnv *, jclass){
    if (g_ecamMgr==NULL)return -1;
    int req = ecam_stream_req_get_transfer_method(g_ecamMgr->req);
    switch (req){
        case 0:
            return 0;//other
        case 1:
            return 3;//upnp
        case 2:
        {
            int ice_flag = ice_get_type(ecam_stream_req_get_ice(g_ecamMgr->req));
            if (ice_flag==0){
                return 0;//other
            } else if(ice_flag == 1){
                return 2;//stun
            }else if (ice_flag == 2){
                return 1;//turn
            }
        }
            break;
        default:
            break;
    }
    return -1;
}

JNIEXPORT jlongArray JNICALL Java_com_howell_jni_JniUtil_ecamGetSdpTime
        (JNIEnv *env, jclass){
    if(g_ecamMgr == NULL)return NULL;
    if(g_ecamMgr->req == NULL) return NULL;
    time_t beg=0;
    time_t end=0;
    ecam_stream_req_get_sdp_time(g_ecamMgr->req,&beg,&end);
    jlong * _arry = new jlong[2];
    _arry[0] = beg;
    _arry[1] = end;

    //  LOGI(" ecam_stream_req_get_sdp_time beg=%ld  end=%ld\n",beg,end);
    jlongArray longArray = env->NewLongArray(2);
    env->ReleaseLongArrayElements(longArray,_arry,JNI_COMMIT);
    delete [] _arry;
    return longArray;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_ecamSendAudioData
        (JNIEnv *env, jclass, jbyteArray bytes, jint len){
    if (g_ecamMgr==NULL)return -1;
    char *data = (char *) env->GetByteArrayElements(bytes, NULL);
    if (data==NULL)return -1;
    int dstlen = 0;
    char *encodeData = (char *) malloc(1024);
    memset(encodeData,0,1024);
    g711u_Encode((unsigned char *) data, (unsigned char *) encodeData, len, (unsigned int *) &dstlen);
    int ret = ecam_stream_send_audio(g_ecamMgr->req,0, encodeData, dstlen, 0);
    //LOGE("ecam_stream_send_audio ret=%d\n",ret);
    env->ReleaseByteArrayElements(bytes, (jbyte *) data, 0);
    free(encodeData);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_ecamGetStreamLenSomeTime
        (JNIEnv *, jclass){
    if (g_ecamMgr==NULL)return -1;
    int len = g_ecamMgr->ecamDataLen;
    g_ecamMgr->ecamDataLen=0;
    return len;
}





















