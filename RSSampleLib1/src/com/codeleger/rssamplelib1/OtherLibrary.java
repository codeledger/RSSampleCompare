package com.codeleger.rssamplelib1;

import android.util.Log; 

public class OtherLibrary {
	final static String TAG = OtherLibrary.class.getName();
	
    static public void waterFilter(int startWidth, int startHeight, int width, int height, int range, int[] inPixels, int[] outPixels) {

        int levels = 256;
        int index = 0;
        
        int[] rHistogram = new int[levels];
        int[] gHistogram = new int[levels];
        int[] bHistogram = new int[levels];
        int[] rTotal = new int[levels];
        int[] gTotal = new int[levels];
        int[] bTotal = new int[levels];

        index = startHeight*width;

//        Log.d(TAG,"waterFilter " + startWidth + " " + startHeight + " " + width + " " + height + " "+ range + "index:"+index);        
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
                                int rgb = inPixels[ioffset+ix];
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

                outPixels[index] = (inPixels[index] & 0xff000000) | ( r << 16 ) | ( g << 8 ) | b;
                index++;
            }
        }

    }
    
    
    public OtherLibrary() {
    	
    }
    
}
