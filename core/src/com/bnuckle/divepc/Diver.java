package com.bnuckle.divepc;

import com.bnuckle.divepc.pc.ZHL16;

public class Diver
{

    private ZHL16 pc;
    private double depth;
    private double velocity;

    public Diver()
    {
        super();
    }

    public ZHL16 getPc() {
        return pc;
    }

    public void setPc(ZHL16 pc) {
        this.pc = pc;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }



    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void step(double delta)
    {
        depth -= velocity;
        pc.setPressure(depthToPressure(depth));
        pc.step(delta);
    }

    public double depthToPressure(double depth)
    {
        return depth / 10 + 1;
    }

}
