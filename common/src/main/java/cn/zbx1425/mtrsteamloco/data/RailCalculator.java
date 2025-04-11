package cn.zbx1425.mtrsteamloco.data;

import mtr.data.RailAngle;
import net.minecraft.core.BlockPos;

public class RailCalculator {
    public static double PRECISION = 1e-5;

    public static class Vec2 {
        private final double x;
        private final double y;

        public Vec2(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Vec2 add(Vec2 other) {
            return new Vec2(this.x + other.x, this.y + other.y);
        }

        public Vec2 sub(Vec2 other) {
            return new Vec2(this.x - other.x, this.y - other.y);
        }

        public Vec2 scale(double factor) {
            return new Vec2(this.x * factor, this.y * factor);
        }

        public Vec2 scale(double factorX, double factorY) {
            return new Vec2(this.x * factorX, this.y * factorY);
        }

        public double length() {
            return Math.sqrt(this.x * this.x + this.y * this.y);
        }

        public Vec2 normalize() {
            double length = length();
            return new Vec2(this.x / length, this.y / length);
        }

        public Vec2 rotate(float deg) {
            double rad = Math.toRadians(deg);
            return rotate(rad);
        }

        public Vec2 rotate(double rad) {
            rad = -rad;
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);
            return new Vec2(this.x * cos - this.y * sin, this.x * sin + this.y * cos);
        }

        public float deg() {
            return (float) Math.toDegrees(rad());
        }

        public double rad() {
            return Math.atan2(this.y, this.x);
        }

        public double distance(Vec2 other) {
            return Math.sqrt((this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (o instanceof Vec2 d) {
                return Math.abs(this.x - d.x) < PRECISION && Math.abs(this.y - d.y) < PRECISION;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("(%.2f, %.2f)", x, y);
        }
    }

    public static class Section {
        public final double h, k, r, tStart, tEnd;
        public final boolean reverseT, isStraight;

        public Section(double h, double k, double r, double tStart, double tEnd, boolean reverseT, boolean isStraight) {
            this.h = h;
            this.k = k;
            this.r = r;
            this.tStart = tStart;
            this.tEnd = tEnd;
            this.reverseT = reverseT;
            this.isStraight = isStraight;
        }
    }

    public static class Group {
        public final Section first;
        public final Section second;

        public Group(Section first, Section second) {
            this.first = first;
            this.second = second;
        }
    }

    public static interface Shape {
        public Section toSection();
    }


    public static class Arc implements Shape {
        private final Vec2 center, start, end;
        private final double radius;

        public Arc(Vec2 center, double radius, Vec2 start, Vec2 end) {
            this.center = center;
            this.radius = radius;
            this.start = start;
            this.end = end;
        }

        @Override
        public Section toSection() {
            double r = radius;
            double h = center.x;
            double k = center.y;

            Vec2 vCS = start.sub(center);
            Vec2 vCE = end.sub(center);
            double tStart = vCS.rad() * r;
            double tEnd = vCE.rad() * r;

            boolean reverseT = tStart > tEnd;

            return new Section(h, k, r, tStart, tEnd, reverseT, false);
        }
    }

    public static class Segment implements Shape {
        private final Vec2 start;
        private final Vec2 end;

        public Segment(Vec2 start, Vec2 end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public Section toSection() {
            
            Vec2 v = end.sub(start);
            double l = v.length();
            double h = v.x / l;// cos
            double k = v.y / l;// sin

            boolean useStandardFormula = Math.abs(h) >= 0.5 && Math.abs(k) >= 0.5;
            double r, tStart, tEnd;

            double x1 = start.x, z1 = start.y;
            double x2 = end.x, z2 = end.y;
            
            if (useStandardFormula) {
                // 适用于非纯水平/垂直的斜线
                r = (h * z1 - k * x1) / (h * h);
                tStart = x1 / h;
                tEnd = x2 / h;
            } else {
                // 处理水平或垂直线段
                final double div = 2 * h * k; // 方向向量叠加后的cos值[4](@ref)
                r = (h * z1 - k * x1) / div;
                tStart = (h * x1 - k * z1) / div;
                tEnd = (h * x2 - k * z2) / div;
            }

            boolean reverseT = tStart > tEnd;

            return new Section(h, k, r, tStart, tEnd, reverseT, true);
        }
    }

    public static class Line {
        private final double A;
        private final double B;
        private final double C;

        public Line(double a, double b, double c) {
            this.A = a;
            this.B = b;
            this.C = c;
        }

        public Line(Vec2 p1, Vec2 p2) {
            this(p1.y - p2.y, p2.x - p1.x, p1.x * p2.y - p2.x * p1.y);
        }

        /**​
        * 计算两直线交点
        * @param other 另一条直线
        * @return 交点坐标（若平行则返回null）
        */
        public Vec2 intersection(Line other) {
            double denominator = this.A * other.B - other.A * this.B;
            // 处理浮点数精度误差
            if (Math.abs(denominator) < 1e-9) {
                return null; // 平行或重合
            }
            double x = (this.B * other.C - other.B * this.C) / denominator;
            double y = (other.A * this.C - this.A * other.C) / denominator;
            return new Vec2(x, y);
        }

        public Line perpendicular(Vec2 p) {
            double a = -this.B;
            double b = this.A;
            double c = -a * p.x - b * p.y;
            return new Line(a, b, c);
        }

        @Override
        public String toString() {
            return String.format("%.2fx + %.2fy + %.2f = 0", A, B, C);
        }
    }

    private static void pl(String s) {
        System.out.println("Infomation in RailCalculator: " + s);
    }

    public static Group calculator(BlockPos startPos, BlockPos endPos, RailAngle startAngle, RailAngle endAngle) {
        Vec2 A = new Vec2(startPos.getX(), startPos.getZ());
        Vec2 B = new Vec2(endPos.getX(), endPos.getZ());

        Vec2 vAC = new Vec2(1, 0).rotate(startAngle.angleDegrees).normalize();
        Vec2 C = vAC.add(A);
        Line AC = new Line(A, C);

        Vec2 vBD = new Vec2(1, 0).rotate(endAngle.angleDegrees).normalize();
        Vec2 D = vBD.add(B);
        Line BD = new Line(B, D);
        
        Vec2 E = AC.intersection(BD);

        if (E == null) {
            pl("无交点");
            return null;
        }
        pl("交点: " + E);

        /* 
        Vec2 vAE = E.sub(A).normalize();
        boolean b1 = vAE.equals(vAC);

        Vec2 vBE = E.sub(B).normalize();
        boolean b2 = vBE.equals(vBD);

        pl(b1 + " " + b2);

        if (!b1 || !b2) {
            pl("不在射线上" + vAE + " " + vBE + " " + vAC + " " + vBD);
            return null;
        }
        */
       
        double ra = E.distance(A);
        double rb = E.distance(B);

        boolean flag = ra > rb;
        double r = flag ? rb : ra;

        pl("直线在前：" + flag);
        pl("半径: " + r + " ( " + ra + ", " + rb + " )");

        Vec2 vEA = A.sub(E);
        Vec2 vEB = B.sub(E);
        float a1 = vEB.deg() - vEA.deg();

        Line AE = new Line(A, E);
        Line BE = new Line(B, E);

        Line l1, l2;
        Vec2 F;
        if (!flag) {
            l1 = AE.perpendicular(A);
            
            Vec2 vEF = vEA.rotate(a1);
            F = E.add(vEF);
            l2 = BE.perpendicular(F);
        } else {
            l1 = BE.perpendicular(B);
            
            a1 = -a1;
            Vec2 vEF = vEB.rotate(a1);
            F = E.add(vEF);
            l2 = AE.perpendicular(F);
        }

        Vec2 O = l1.intersection(l2);
        if (O == null) {
            pl("无圆心");
            return null;
        }
        pl("圆心: " + O);

        double h1, k1, r1, tStart1, tEnd1;
        double h2, k2, r2, tStart2, tEnd2;
        boolean reverseT1, isStraight1, reverseT2, isStraight2;

        Shape s1, s2;
        if (!flag) {
            s1 = new Arc(O, r, A, F);
            s2 = new Segment(F, B);
        } else {
            s2 = new Arc(O, r, B, F);
            s1 = new Segment(F, A);
        }

        Section sect1 = s1.toSection();
        Section sect2 = s2.toSection();

        return new Group(sect1, sect2);
    }
}