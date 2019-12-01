//
// Created by Randall Smith on 2019-11-17.
// This implementation of the Stitching Algorithm is heavily influenced by Adrian Rosebrock
// You can see his code at: https://www.pyimagesearch.com/2018/12/17/image-stitching-with-opencv-and-python/
//

#include "dev_rjsmith_panorama_NativePanorama.h"
#include "opencv2/opencv.hpp"
#include "opencv2/stitching.hpp"

using namespace std;
using namespace cv;

JNIEXPORT void JNICALL Java_com_example_panorama_NativePanorama_processPanorama
  (JNIEnv * env, jclass clazz, jlongArray imageAddressArray, jlong outputAddress){

  jsize a_len = env->GetArrayLength(imageAddressArray);

  jlong *imgAddressArr = env->GetLongArrayElements(imageAddressArray,0);

  vector< Mat > imgVec;
  for(int k=0;k<a_len;k++)
  {

    Mat & curimage=*(Mat*)imgAddressArr[k];
    Mat newimage;

    cvtColor(curimage, newimage, CV_BGRA2RGB);

    float scale = 1000.0f / curimage.rows;
    resize(newimage, newimage, Size(scale * curimage.rows, scale * curimage.cols));
    imgVec.push_back(newimage);
  }
  Mat & result  = *(Mat*) outputAddress;
  Stitcher stitcher = Stitcher::createDefault();
  stitcher.stitch(imgVec, result);

  env->ReleaseLongArrayElements(imageAddressArray, imgAddressArr ,0);
}