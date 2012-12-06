#pragma version(1)
#pragma rs java_package_name(com.codeledger.rssamplelib1)

rs_allocation gIn;
rs_allocation gOut;
rs_script gScript;
int width;
int height;

const static int levels = 256;
const static int range = 5;

void root(const uchar4 *v_in, uchar4 *v_out, const void *usrData, uint32_t x, uint32_t y) {

	uint32_t rHistogram [levels];
	uint32_t gHistogram [levels];
	uint32_t bHistogram [levels];
	int rTotal [levels];
	int gTotal [levels];
	int bTotal [levels];
	int i;
	int j;

	float4 pixel_in = rsUnpackColor8888(*v_in);

	for ( int i = 0; i < levels; i++)
		rHistogram[i] = gHistogram[i] = bHistogram[i] = rTotal[i] = gTotal[i] = bTotal[i] = 0;

	for ( int row = -range; row <= range; row++)
	{
		int iy = y+row;
		int ioffset;
		if (0 <= iy && iy < height)
		{
			ioffset = iy*height;
			for ( int col = -range; col <= range; col++)
			{
				int ix = x+col;
				if (0 <= ix && ix < width) {

					uchar4 *  rgb_ref = (uchar4 *) rsGetElementAt(gIn,ix,iy);
					uchar4 rgb = *rgb_ref;

					// assume RGBA
					uchar r = rgb.r;
					uchar g = rgb.g;
					uchar b = rgb.b;
					uchar alpha = rgb.a;

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
	for ( j = 1; j < levels; j++)
	{
		if (rHistogram[j] > rHistogram[r])
			r = j;
		if (gHistogram[j] > gHistogram[g])
			g = j;
		if (bHistogram[j] > bHistogram[b])
			b = j;
	}
	r = rTotal[r] / rHistogram[r];
	g = gTotal[g] / gHistogram[g];
	b = bTotal[b] / bHistogram[b];

	if((x == 200) && (y == 200)) {
		rsDebug("r",r);
		rsDebug("g",g);
		rsDebug("b",b);
	}

	uchar4 outPixel;

	outPixel.s0 = r;
	outPixel.s1 = g;
	outPixel.s2 = b;
	outPixel.s3 = 0xff;


	*v_out = outPixel;
}

void doWaterFilter() {
	rsDebug("Water Filter start" ,0);
	rsDebug("Water height",height);
	rsDebug("Water width",width);
    rsForEach(gScript, gIn, gOut,0,0);
    rsDebug("Water Filter end",0);
}

