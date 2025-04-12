package cn.zbx1425.mtrsteamloco.data;

import java.util.List;
import java.util.ArrayList;

public class RailCalculator {
    public static final double PRECISION = 1e-6;

    public static class Vec2 {
        public final double x, z;

        public Vec2(double x, double z) {
            this.x = x;
            this.z = z;
        }

        public Vec2 add(Vec2 other) {
            return new Vec2(x + other.x, z + other.z);
        }

        public Vec2 sub(Vec2 other) {
            return new Vec2(x - other.x, z - other.z);
        }

        public Vec2 rotateDeg(double angle) {
            return rotateRad(Math.toRadians(angle));
        }

        public Vec2 rotateRad(double angle) {
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            return new Vec2(x * cos - z * sin, x * sin + z * cos);
        }

        public double length() {
            return Math.sqrt(x * x + z * z);
        }

        public double distance(Vec2 other) {
            return sub(other).length();
        }

        public double radian() {
            return Math.atan2(z, x);
        }

        public double degree() {
            return Math.toDegrees(radian());
        }

        public double sin() {
            return z / length();
        }

        public double cos() {
            return x / length();
        }

        public String toString() {
            return String.format("Vec2(%.2f, %.2f)", x, z);
        }
    }

    public static class Line {
        public final double A, B, C; // Ax + By + C = 0

        public Line(Vec2 p1, Vec2 p2) {
            A = p1.z - p2.z;
            B = p2.x - p1.x;
            C = p1.x * p2.z - p2.x * p1.z;
        }

        public Vec2 intersection(Line other) {
            double det = A * other.B - other.A * B;
            if (Math.abs(det) < PRECISION) {
                return null;
            }
            double x = (B * other.C - other.B * C) / det;
            double z = (other.A * C - A * other.C) / det;
            return new Vec2(x, z);
        }

        public double distance(Vec2 p) {
            return Math.abs(A * p.x + B * p.z + C) / Math.sqrt(A * A + B * B);
        }

        public Line perpendicular(Vec2 p) {
            Vec2 d = direction().rotateDeg(90);
            return new Line(p, p.add(d));
        }

        public Vec2 direction() {
            return new Vec2(B, -A);
        }

        public String toString() {
            return String.format("Line: %s*x + %s*z + %s = 0", A, B, C);
        }
    }

    public static class Circle {
        public final Vec2 center;
        public final double radius; 

        public Circle(Vec2 center, double radius) {
            this.center = center;
            this.radius = radius;
        }

        public List<Vec2> intersections(Line line) {
            List<Vec2> result = new ArrayList<>();
            double d = line.distance(center);
            if (d > radius + PRECISION) {
                return result;
            }

            Line perp = line.perpendicular(center);
            Vec2 foot = line.intersection(perp);
            if (foot == null) {
                return result;
            }

            if (Math.abs(d - radius) <= PRECISION) {
                result.add(foot);
                return result;
            }

            double t = Math.sqrt(radius * radius - d * d);
            Vec2 dir = line.direction();
            double dirLength = dir.length();
            if (dirLength < PRECISION) {
                return result;
            }
            Vec2 unitDir = new Vec2(dir.x / dirLength, dir.z / dirLength);
            Vec2 delta = new Vec2(unitDir.x * t, unitDir.z * t);
            result.add(foot.add(delta));
            result.add(foot.sub(delta));
            return result;
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
        
        public double getLength() {
            return Math.abs(tEnd - tStart);
        }

        public boolean isValid() {
            boolean b1 = Double.isFinite(h);
            boolean b2 = Double.isFinite(k);
            boolean b3 = Double.isFinite(r);
            boolean b4 = Double.isFinite(tStart);
            boolean b5 = Double.isFinite(tEnd);
            pl("isValid: " + b1 + " " + b2 + " " + b3 + " " + b4 + " " + b5);
            return b1 && b2 && b3 && b4 && b5;
        }
    }

    public static interface Shape {
        Section toSection();
    }

    public static class Arc implements Shape {
        private final Vec2 center, start, mid, end;
        private final double radius;

        public Arc(Vec2 center, Vec2 start, Vec2 mid, Vec2 end) {
            this.center = center;
            this.radius = center.distance(start);
            this.start = start;
            this.mid = mid;
            this.end = end;
        }

        @Override
        public Section toSection() {
            double h = this.center.x;
            double k = this.center.z;
            double r = this.radius;

            // 计算起点、中点、终点相对于圆心的极角（弧度）
            Vec2 sRel = start.sub(center);
            double thetaS = sRel.radian();
            thetaS = (thetaS + 2 * Math.PI) % (2 * Math.PI); // 归一化到0~2π

            Vec2 mRel = mid.sub(center);
            double thetaM = mRel.radian();
            thetaM = (thetaM + 2 * Math.PI) % (2 * Math.PI);

            Vec2 eRel = end.sub(center);
            double thetaE = eRel.radian();
            thetaE = (thetaE + 2 * Math.PI) % (2 * Math.PI);

            // 计算从起点到终点的逆时针角度差
            double delta_ccw = (thetaE - thetaS + 2 * Math.PI) % (2 * Math.PI);

            // 计算中间点相对于起点的逆时针角度差
            double delta_m_ccw = (thetaM - thetaS + 2 * Math.PI) % (2 * Math.PI);

            // 判断中间点是否位于逆时针路径上
            boolean isCCW = delta_m_ccw <= delta_ccw;
            boolean reverseT = isCCW;

            // 计算起始和结束弧长参数
            double tStart = thetaS * r;
            double tEnd = thetaE * r;

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
            Vec2 delta = end.sub(start);
            double len = delta.length();
            if (len < PRECISION) {
                // 无效的线段，返回无效的Section
                return new Section(Double.NaN, Double.NaN, Double.NaN, 0, 0, false, true);
            }
            double h = delta.x / len; // 单位方向向量的x分量
            double k = delta.z / len; // 单位方向向量的z分量
            boolean isForm1 = Math.abs(h) >= 0.5 && Math.abs(k) >= 0.5;

            double r, tStart, tEnd;
            if (isForm1) {
                // 形式1: x = h*T, z = k*T + h*r
                r = (h * start.z - k * start.x) / (h * h);
                tStart = start.x / h;
                tEnd = end.x / h;
            } else {
                // 形式2: x = h*T + k*r, z = k*T + h*r
                double div = 2 * h * h - 1; // 等于h² - k²，因为h² + k²=1
                r = (h * start.z - k * start.x) / div;
                tStart = (h * start.x - k * start.z) / div;
                tEnd = (h * end.x - k * end.z) / div;
            }

            boolean reverseT = tStart > tEnd;
            return new Section(h, k, r, tStart, tEnd, reverseT, true);
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

    public static void pl(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) {
        double startX = -4;
        double startZ = 3;
        double endX = 4;
        double endZ = 1;
        double startAngle = -45;
        double endAngle = -135;

        Group group = calculate(startX, startZ, endX, endZ, startAngle, endAngle);
    }

    public static Group calculate(double startX, double startZ, double endX, double endZ, double startAngle, double endAngle) {
        Group group = _calculate(startX, startZ, endX, endZ, startAngle, endAngle);

        if (group == null) return null;
        if (group.first.isValid() && group.second.isValid()) {
            pl(group.first.getLength() + " " + group.second.getLength());
            return group;
        }
        pl("无效的section");
        return null;
    }

    private static Group _calculate(double startX, double startZ, double endX, double endZ, double startAngle, double endAngle) {
        Vec2 S = new Vec2(startX, startZ);
        double alpha = startAngle;
        Vec2 E = new Vec2(endX, endZ);
        double beta = endAngle;
        pl ("S " + S + " alpha " + alpha + " E " + E + " beta " + beta);

        if (false) {
            Vec2 temp = S;
            S = E;
            E = temp;
            double temp2 = alpha;
            alpha = beta;
            beta = temp2;
        }

        Vec2 vSS1 = new Vec2(1, 0).rotateDeg(alpha);
        Vec2 S1 = S.add(vSS1);

        Vec2 vEE1 = new Vec2(1, 0).rotateDeg(beta);
        Vec2 E1 = E.add(vEE1);

        Line SS1 = new Line(S, S1);
        Line EE1 = new Line(E, E1);

        Vec2 M = SS1.intersection(EE1);

        if (M == null) {
            pl("无交点");
            return null;
        }
        pl("交点：" + M);

        Vec2 vMS = S.sub(M);
        Vec2 vME = E.sub(M);
        double theta = vME.degree() - vMS.degree();
        double dME = M.distance(E);
        double dMS = M.distance(S);

        if (dME > dMS) { // 曲线在前
            pl("曲线在前");

            Line p1 = SS1.perpendicular(S);

            Vec2 vMF = vMS.rotateDeg(theta);
            Vec2 F = M.add(vMF);
            Line p2 = EE1.perpendicular(F);

            Vec2 O = p1.intersection(p2);

            if (O == null) {
                pl("无交点");
                return null;
            }
            pl("O " + O);

            Circle c = new Circle(O, dMS);
            Line OM = new Line(O, M);
            List<Vec2> intersections = c.intersections(OM);
            Vec2 H = null;
            for (Vec2 p : intersections) {
                if (H == null) {
                    H = p;
                    continue;
                }
                if (p.distance(E) < H.distance(E)) {
                    H = p;
                }
            }
            if (H == null) {
                pl("无交点");
                return null;
            }
            pl("H " + H);

            Arc arc = new Arc(O, S, H, F);
            Segment seg = new Segment(F, E);
            Group group = new Group(arc.toSection(), seg.toSection());
            return group;
        } else {
            pl("曲线在后");

            Line p1 = EE1.perpendicular(E);

            Vec2 vMF = vME.rotateDeg(-theta);
            Vec2 F = M.add(vMF);
            Line p2 = SS1.perpendicular(F);
            
            Vec2 O = p1.intersection(p2);
            if (O == null) {
                pl("无交点");
                return null;
            }
            pl("O " + O);

            Circle c = new Circle(O, dME);
            Line OM = new Line(O, M);
            List<Vec2> intersections = c.intersections(OM);
            Vec2 H = null;
            for (Vec2 p : intersections) {
                if (H == null) {
                    H = p;
                    continue;
                }
                if (p.distance(S) < H.distance(S)) {
                    H = p;
                }
            }
            if (H == null) {
                pl("无交点");
                return null;
            }
            pl("H " + H);

            Segment seg = new Segment(S, F);
            Arc arc = new Arc(O, F, H, E);
            Group group = new Group(seg.toSection(), arc.toSection());
            return group;
        }
    }
}