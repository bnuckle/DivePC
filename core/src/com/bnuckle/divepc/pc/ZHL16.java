/*
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bnuckle.divepc.pc;
/**
 * Emulates a dive computer using the ZHL16A algorithm
 * using the experimental values for a
 * Does not support diving at altitude
 */
public class ZHL16
{


    //Half times, and a and b values for nitrogen and helium
    private final double[][] halftimes =
            {   //       Nitrogen                 Helium
                //  half    a       b       half    a       b
                    {4.0,   1.2599, .5050,  1.5,    1.7435, .1911},
                    {8,     1,      .6514,  3,      1.3838, .4295,},
                    {12.5,  .8618,  .7222,  4.7,    1.1925, .5446},
                    {18.5,  .7562,  .7725,  7,      1.0465, .6265},
                    {27,    .6667,  .8125,  10.2,   .9226,  .6917},
                    {38.3,  .5933,  .8434,  14.5,   .8211,  .7420},
                    {54.3,  .5282,  .8693,  20.5,   .7309,  .7841},
                    {77,    .4701,  .891,   29.1,   .6506,  .8195},
                    {109,   .4187,  .9092,  41.1,   .5794,  .8491},
                    {146,   .3798,  .9222,  55.1,   .5256,  .8703},
                    {187,   .3497,  .9319,  70.6,   .4840,  .8860},
                    {239,   .3223,  .9403,  90.2,   .446,   .8997},
                    {305,   .2971,  .9477,  115.1,  .4112,  .9118},
                    {390,   .2737,  .9544,  147.2,  .3788,  .9226},
                    {498,   .2523,  .9602,  187.9,  .3492,  .9321},
                    {635,   .2327,  .9553,  239.6,  .3220,  .9404}
            };

    //matrix for calculating bottom time from M subzero and Delta M
    private final double[][] mParts =
            {       //M0    //dM
                    {106.4, 1.9082},
                    {83.2,  1.5352},
                    {73.8,  1.3847},
                    {66.8,  1.2780},
                    {62.3,  1.2306},
                    {58.5,  1.1857},
                    {55.2,  1.1504},
                    {77,    1.1223},
                    {109,   1.0999},
                    {146,   1.0844},
                    {187,   1.0731},
                    {239,   1.0635},
                    {305,   1.0552},
                    {390,   1.0478},
                    {498,   1.0414},
                    {635,   1.0359}
            };

    private double percentN2, percentHe;

    private double[] compartmentN2 = new double[16];
    private double[] compartmentH = new double[16];

    private double depth;

    private double NDL;

    /**
     * constructs a dive computer set up for diving air (EAN21 - 21% air 79% nitrogen 0% helium) at depth 0
     * same as new ZHL16(79,0,0)
     */
    public ZHL16()
    {
        this(79, 0, 0);
    }

    /**
     * constructs a dive computer at depth 0
     *@param percentN2 the percent of nitrogen in gas mixture
     *@param percentHe the percent of helium in gas mixture
     */
    public ZHL16(double percentN2, double percentHe)
    {
        this(percentN2, percentHe, 0);
    }

    /**
     * constructs a dive computer
     * @param percentN2 the percent of nitrogen in gas mixture
     * @param percentHe the percent of helium in gas mixture
     * @param depth the depth to start at
     */
    public ZHL16(double percentN2, double percentHe, double depth)
    {
        this.percentN2 = percentN2 / 100;
        this.percentHe = percentHe / 100;

        resetCompartments();

        NDL = (halftimes[0][0] /6.93) * Math.log((compartmentH[0]-depthToATM(depth))/mValue(depth) - depthToATM(depth) );

    }

    public void resetCompartments()
    {
        for(int i = 0; i < 16; i++)
        {
            compartmentN2[i] = percentN2 * depthToATM(depth);
            compartmentH[i] = percentHe * depthToATM(depth);
        }
    }


    /**
     * calculates compartments
     * @param delta the time exposed in seconds
     */
    public void step(double delta)
    {

        //converts delta seconds into delta minutes
        double te = delta / 60;

        //update each compartment
        for(int i = 0; i < 16; i++)
        {
            //Nitrogen
            if(percentN2 != 0)
            {
                double ntht = halftimes[i][0]; //Half-time of the compartment (minutes)
                double nPGas = depthToATM(depth) * percentN2;
                compartmentN2[i] = compartmentN2[i] + ((nPGas - compartmentN2[i]) * (1 - Math.pow(2, (-1 * te / ntht))));

                NDL = Math.min(NDL, (halftimes[i][0] / 6.93) * Math.log((compartmentH[0] - depthToATM(depth)) / mValue(depth, i) - depthToATM(depth)));
            }

            //Helium
            if (percentHe != 0)
            {
                double htht = halftimes[i][3]; //Half-time of the compartment (minutes)
                double hPGas = depthToATM(depth) * percentHe;
                compartmentH[i] = compartmentH[i] + ((hPGas - compartmentH[i]) * (1 - Math.pow(2, (te / htht))));

                NDL = Math.min(NDL, (halftimes[i][3] / 6.93) * Math.log((compartmentH[0] - depthToATM(depth)) / mValue(depth, i) - depthToATM(depth)));
            }
        }
    }

    /**
     * converts depth to pressure
     * @param feet depth underwater
     * @return pressure
     */
    private double depthToATM(double feet)
    {
        return (feet + 33) / 33 ;
    }

    /**
     * sets depth of pc
     * @param d the depth IN FEET!!!!
     */
    public void goToDepth(double d)
    {
        depth = d;
    }

    /**
     * converts meters to feet, useful for goToDepth() and ftToATM() because I use freedom units
     * @param meter the freedomless unit of length
     * @return the freedomful unit of length
     */
    private double meterToFt(double meter)
    {
        return meter * 3.281;
    }

    /**
     * prints the compartments along with their partial pressures
     */
    public void printCompartments()
    {
        System.out.println(toString());
    }

    /**
     * returns a string representation of the compartments along with their partial pressures
     * @return the string representation
     */
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Compartments\nN2          He\n");
        for(int i = 0; i < 16; i++)
        {
            result.append(String.format("%f | %f%n", compartmentN2[i], compartmentH[i]));
        }
        return result.toString();
    }

    /**
     * calculates the m value for each compartment depending on the depth of the diver for the pc
     * @param depth the depth of the pc
     * @param c the compartment/index of the tissue, offset by 1 since compartment 1 is index 0
     * @return the m value
     */
    private double mValue(double depth, int c)
    {
        return mParts[1][c] * depth + mParts[0][c];
    }

}
