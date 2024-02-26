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
package com.bnuckle.divepc;

/**
 * Emulates a dive computer using the ZHL16 algorithm
 */
public class ZHL16
{


    //Nitrogen
    private double  nPBegin, //Inert gas pressure in the compartment before the exposure time (ATM)
                    nPGas,   //Inert gas pressure in the mixture being breathed (ATM)
                    ntht;    //Half-time of the compartment (minutes)

    //Helium
    private double  hPBegin, //Inert gas pressure in the compartment before the exposure time (ATM)
                    hPGas,   //Inert gas pressure in the mixture being breathed (ATM)
                    htht;    //Half-time of the compartment (minutes)

    private double pAmbtol; //Pressure you could drop to (ATM)

    // Pcomp = Pbegin + [ Pgas - Pbegin ] x [ 1 - 2 ^ ( - te / tht ) ]

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

    private double percentN2, percentHe;

    private double[] compartmentN2 = new double[16];
    private double[] compartmentH = new double[16];

    private double depth;

    /**
     * constructs a dive computer set up for diving air (EAN21 - 21% air 79% nitrogen 0% helium) at depth 0
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
        this.percentN2 = percentN2;
        this.percentHe = percentHe;

        nPBegin = percentN2;
        hPBegin = percentHe;

        for(int i = 0; i < 16; i++)
        {
            compartmentN2[i] = percentN2 * ftToATM(depth);
            compartmentH[i] = percentHe * ftToATM(depth);
        }
    }

    /**
     * calculates compartments
     * @param te the time exposed in seconds
     */
    public void step(double te)
    {

        te /= 60;

        //update each compartment
        for(int i = 0; i < 16; i++)
        {
            //Nitrogen

            //Pcomp = Pbegin + [ Pgas - Pbegin ] x [ 1 - 2 ^ ( - te / tht ) ]
            nPBegin = compartmentN2[i];
            ntht = halftimes[i][0];
            compartmentN2[i] = nPBegin + ((nPGas - nPBegin) * ( 1 - Math.pow(2 , ( te / ntht ) ) ) );


            //Helium
            if (percentHe == 0) continue;

            //Pcomp = Pbegin + [ Pgas - Pbegin ] x [ 1 - 2 ^ ( - te / tht ) ]
            hPBegin = compartmentH[i];
            htht = halftimes[i][3];
            compartmentH[i] = hPBegin + ((hPGas - hPBegin) * ( 1 - Math.pow(2 , ( te / htht ) ) ) );
        }
    }

    /**
     * converts depth to pressure
     * @param feet depth underwater
     * @return pressure
     */
    private double ftToATM(double feet)
    {
        if(feet == 0) return 1;
        return feet / 33 + 1;
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
     * prints
     */
    public void printCompartments()
    {
        System.out.println("Compartments\nN2          He");
        for (int i = 0; i < 16; i++)
        {
            System.out.printf("%f | %f%n", compartmentN2[i], compartmentH[i]);
        }
    }

}
