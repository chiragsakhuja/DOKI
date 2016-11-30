package com.mobilecomputing.dokimobile;

/**
 * Created by kammi on 11/24/2016.
 */

public class Signal {

    public static double val_match_thr;

    short[] data;

    Signal()	{data = new short[15];}

    public	String	toString()
    {
        String line = "";

        for(int i=0; i<15; i++)	{ line += Integer.toString(data[i]) + "\t"; }

        return line;
    }

    void	add_noise( int amount)
    {
        for(int i=0; i<amount; i++) { data[i] = 0; }
    }

    Boolean match(Signal rhs)	{return match(rhs,15);}

    Boolean in_range(short val, short target)
    {
        return ((double)(Math.abs(val-target)) <= Math.abs(target * val_match_thr));
    }

    Boolean match(Signal rhs, int threshold)
    {
        if( (threshold < 1) || (threshold > 15) )
        {
            //System.out.println("Threshold should be between 1 and 15");
            return false;
        }

        int match_count = 0;

        for(int i=0; i<15; i++)
            if( in_range(rhs.data[i],data[i]) )
                match_count++;

        return (match_count>=threshold);
    }
}
