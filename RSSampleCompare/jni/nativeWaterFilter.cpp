#include <jni.h>

namespace com_codeledger_rssamplecompare_NativeWaterFilter {

	static void nativeWaterFilter(JNIEnv *env, jclass clazz, jint startWidth, jint startHeight, jint width, jint height, jint range, jintArray inPixels, jintArray outPixels) {
	    int levels = 256;
	    int index = 0;

	    int* rHistogram = new int[levels];
	    int* gHistogram = new int[levels];
	    int* bHistogram = new int[levels];

	    int* rTotal = new int[levels];
	    int* gTotal = new int[levels];
	    int* bTotal = new int[levels];

	    index = startHeight*width;

	    int* inData = env->GetIntArrayElements(inPixels, 0);
	    int* outData = env->GetIntArrayElements(outPixels, 0);

	    for (int y = startHeight; y < height; y++)
	    {
	        for (int x = startWidth; x < width; x++)
	        {
	            for (int i = 0; i < levels; i++)
	                rHistogram[i] = gHistogram[i] = bHistogram[i] = rTotal[i] = gTotal[i] = bTotal[i] = 0;

	            for (int row = -range; row <= range; row++)
	            {
	                int iy = y+row;
	                int ioffset;
	                if (0 <= iy && iy < height)
	                {
	                    ioffset = iy*width;
	                    for (int col = -range; col <= range; col++)
	                    {
	                        int ix = x+col;
	                        if (0 <= ix && ix < width) {
	//                            int rgb = inPixels[ioffset+ix];
	                        	int rgb = inData[ioffset+ix];
	                            int r = (rgb >> 16) & 0xff;
	                            int g = (rgb >> 8) & 0xff;
	                            int b = rgb & 0xff;

	                            int ri = r*levels/256;
	                            int gi = g*levels/256;
	                            int bi = b*levels/256;
	                            rTotal[ri] += r;
	                            gTotal[gi] += g;
	                            bTotal[bi] += b;
	                            rHistogram[ri]++;
	                            gHistogram[gi]++;
	                            bHistogram[bi]++;
	                        }
	                    }
	                }
	            }

	            int r = 0, g = 0, b = 0;
	            for (int i = 1; i < levels; i++)
	            {
	                if (rHistogram[i] > rHistogram[r])
	                    r = i;
	                if (gHistogram[i] > gHistogram[g])
	                    g = i;
	                if (bHistogram[i] > bHistogram[b])
	                    b = i;
	            }
	            r = rTotal[r] / rHistogram[r];
	            g = gTotal[g] / gHistogram[g];
	            b = bTotal[b] / bHistogram[b];

	            outData[index] = (inData[index] & 0xff000000) | ( r << 16 ) | ( g << 8 ) | b;
	            index++;
	        }

	    }
        env->ReleaseIntArrayElements(inPixels, inData, 0);
        env->ReleaseIntArrayElements(outPixels, outData, 0);
	}

	static JNINativeMethod method_table[] =
	{
			{"nativeWaterFilter","(IIIII[I[I)V",(void*) nativeWaterFilter }
	};

	static int method_table_size = sizeof(method_table) / sizeof(method_table[0]);
}

using namespace com_codeledger_rssamplecompare_NativeWaterFilter;

extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	  JNIEnv* env;
	  if(vm->GetEnv(reinterpret_cast<void**>(&env),JNI_VERSION_1_6)!=JNI_OK) {
		  return JNI_ERR;
	  } else {
		  jclass clazz = env->FindClass("com/codeledger/rssamplecompare/NativeWaterFilter");
		  if(clazz) {
		      jint ret = env->RegisterNatives(clazz, method_table, method_table_size);
		      env->DeleteLocalRef(clazz);
		      return ret == 0 ? JNI_VERSION_1_6 : JNI_ERR;
		  } else {
		      return JNI_ERR;
		  }
	  }
}





