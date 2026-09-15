package eapli.aisafe.aircontrolarea.domain.GeographicBoundary;

import eapli.aisafe.aircontrolarea.application.exceptions.ZeroSizeAreaException;

import java.util.List;

public class GeographicBoundaryService {

    public GeographicBoundaryService(){}

    /*
    Gauss Area Formula used; TODO: Document this better
     */
    public float calculateArea(GeographicBoundary geoBound){

        int qtyOfCords = geoBound.getGeoCords().size();
        int step;

        float area = 0;

        for (int i = 0; i < qtyOfCords; i++){
            step = (i + 1) % qtyOfCords;

            area += geoBound.getGeoCords().get(i).getX() * geoBound.getGeoCords().get(step).getY();
            area -= geoBound.getGeoCords().get(i).getY() * geoBound.getGeoCords().get(step).getX();
        }

        if (Math.abs(area) == 0) {
            throw new ZeroSizeAreaException();
        }

        return Math.abs(area);
    }

    public boolean checkCollision(GeographicBoundary boundaryA, GeographicBoundary boundaryB) {
        List<GeographicCoords> poly1 = boundaryA.getGeoCords();
        List<GeographicCoords> poly2 = boundaryB.getGeoCords();

        if (!isOverlappingOnAllAxes(poly1, poly2)) {
            return false;
        }
        return isOverlappingOnAllAxes(poly2, poly1);
    }

    private boolean isOverlappingOnAllAxes(List<GeographicCoords> shape1, List<GeographicCoords> shape2) {
        for (int i = 0; i < shape1.size(); i++) {
            GeographicCoords p1 = shape1.get(i);
            GeographicCoords p2 = shape1.get((i + 1) % shape1.size());

            float axisX = -(p2.getY() - p1.getY());
            float axisY = p2.getX() - p1.getX();

            float[] proj1 = project(shape1, axisX, axisY);
            float[] proj2 = project(shape2, axisX, axisY);

            if (proj1[1] < proj2[0] || proj2[1] < proj1[0]) {
                return false;
            }
        }
        return true;
    }

    private float[] project(List<GeographicCoords> shape, float axisX, float axisY) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;

        for (GeographicCoords p : shape) {
            float dot = (p.getX() * axisX) + (p.getY() * axisY);
            if (dot < min) min = dot;
            if (dot > max) max = dot;
        }
        return new float[]{min, max};
    }

}
