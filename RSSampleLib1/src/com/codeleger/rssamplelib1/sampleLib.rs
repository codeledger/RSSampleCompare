/*
 * Random comment
 */

#pragma version(1)
#pragma rs java_package_name(com.codeledger.rssamplelib1)

float x;
float y;
float *result;

void myMult() {
	*result = (x * y);
	rsDebug("In myMult ",x,y);
}
