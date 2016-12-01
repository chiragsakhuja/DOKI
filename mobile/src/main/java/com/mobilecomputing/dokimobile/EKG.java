package com.mobilecomputing.dokimobile;

/**
 * Created by kammi on 11/24/2016.
 */

import	java.io.*;
import	java.util.*;

public class EKG{

    public static float val_match_thr;

    String info;
    ArrayList<Signal> data;
    ArrayList<Integer> matches;
    BufferedReader in = null;

    EKG()
    {
        in = null;
        data = new ArrayList<Signal>();
        matches = new ArrayList<Integer>();
    }

    EKG(InputStream file)
    {
        data = new ArrayList<Signal>();
        matches = new ArrayList<Integer>();

        try {
            in = new BufferedReader(new InputStreamReader(file, "UTF-8"));
        } catch (IOException e) {
             System.exit(1);
        }
    }

    public void run()
    {
        String str;

        try
        {
            info = in.readLine();

            while((str=in.readLine())!=null)
            {
                Signal val = new Signal();

                String tokens[] = str.split("\\s+");

                for(int j=0; j<15; j++)
                    val.data[j] =  Short.parseShort(tokens[j+2]);

                data.add(val);
            }

            if(in!=null){in.close();}
        }
        catch(IOException e)
        {
            System.exit(2);
        }
    }

    int	size()				{ return data.size(); }

    void	add(Signal s)			{ data.add(s); }

    Signal	read(int i)			{ return data.get(i); }

    String	get_info()			{ return info; }

    Boolean is_valid(int i)			{ return (i>=0)&&(i<data.size()); }

    Integer	count_match(Signal val)		{ return count_match(val,15); }

    ArrayList<Integer> search(Signal val)	{ return search(val,15); }

    Boolean exists(Signal val, int threshold)
    {
        for(int i=0; i<data.size(); i++)
            if(read(i).match(val,threshold)) { return true; }

        return false;
    }

    Integer	count_match(Signal val, int threshold)
    {
        int match_count = 0;

        for(int i=0; i<data.size(); i++)
            if(read(i).match(val,threshold)) { match_count++; }

        return match_count;
    }

    ArrayList<Integer> search(Signal val, int threshold)
    {
        ArrayList<Integer> indices = new ArrayList<Integer>();

        for(int i=0; i<data.size(); i++)
            if(read(i).match(val,threshold)) { indices.add(i); }

        return indices;
    }

    void	init_matches()
    {
        matches.clear();

        for(int i=0; i<data.size(); i++)
            matches.add(i-1);
    }

    Boolean	anyMatch() { return !matches.isEmpty(); }

    Integer get_match() { assert matches.size() == 1; return matches.get(0); }

    void	search_next(Signal val, int threshold)
    {
        ArrayList<Integer> old_matches = new ArrayList<Integer>(matches);

        matches.clear();

        //System.out.println("* "+Integer.toString(old_matches.size()));

        for(int i=0; i<old_matches.size(); i++)
        {
            int index = old_matches.get(i)+1;

            if(!is_valid(index)){continue;}

            if(read(index).match(val,threshold)) { matches.add(index); }
        }
    }
}
