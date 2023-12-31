package CryptoCharts;

import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

// A class that makes it possible to "Zoom" the chart even if one of the axes if Categorical
public class ScrollableChart extends ScrollPane
{
    public ScrollableChart(LineChart linechart, int window)
    {
        width = viewPortWidth + window * tickWidth;
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        chart = linechart;
        setContent(linechart);
        setPrefViewportHeight(viewPortHeight);
        setPrefViewportWidth(viewPortWidth);
        linechart.setMaxSize(viewPortWidth, viewPortHeight);

        setOnScroll(event -> {
            //TODO Scale number axis according to the zoom 
            NumberAxis numAxis = (NumberAxis)chart.getYAxis();
            double zoomValue = event.getDeltaY() / width * zoomingSpeed;
            if (currentZoomFactor < 1 && currentZoomFactor > 0
                    || currentZoomFactor >= 1 && zoomValue < 0
                    || currentZoomFactor <= 0 && zoomValue > 0)
                currentZoomFactor += zoomValue;
            setHvalue(1 - currentZoomFactor);    
            double newWidth = width * currentZoomFactor;
            linechart.setMinWidth(newWidth < width ? newWidth : width);
        
        // Save the position of point when mouse was pressed on the chart
        chart.setOnMousePressed(pressEvent ->
        {
            double x = pressEvent.getScreenX();
            double y = pressEvent.getScreenY();
            dragPosition = new Point2D(x, y);
        });
        // When dragged, move the bars of the scroll pane
        chart.setOnMouseDragged(dragEvent ->
        {
            if (dragPosition == null) return;
            double deltaX = dragPosition.getX() - dragEvent.getScreenX(); 
            double deltaY = dragPosition.getY() - dragEvent.getScreenY();
            setHvalue(getHvalue() + deltaX * dragStrength);
            setVvalue(getVvalue() + deltaY * dragStrength * extraYdragStrength);
            dragPosition = new Point2D(dragEvent.getScreenX(), dragEvent.getScreenY());
        });
            // TODO Make the price axis fixed!!!
            //double scrollValue = scrollPane.getHvalue();
            //double xPos = -scrollValue * newWidth + viewPortWidth;            
            //System.out.println("Scroll: " + scrollValue + " XPos: " + xPos);
            //yAxis.setTranslateX(xPos);
        });
    }

    public void setSize(double width, double height)
    {
        viewPortWidth = width;
        viewPortHeight = height;
        setPrefViewportWidth(width);
        setPrefViewportHeight(height);
        chart.setMaxSize(width, height);      
        chart.setPrefSize(width, height);        
        setMaxSize(viewPortWidth, viewPortHeight);
    }
    // TODO fix - doesn't work properly
    public void resetZoom()
    {
        setHvalue(0);
        currentZoomFactor = 0.1;
        chart.setMinWidth(width * currentZoomFactor);
    }

    public void setStrokeColor(String color)
    {        
        for (int i = 0; i < chart.getData().size(); i++)
        {
            XYChart.Series data = (XYChart.Series) chart.getData().get(i);
            data.getNode().setStyle("-fx-stroke:\"" + color + "\";");
        }
    }

    public void setBackgroundColor(String color)
    {
        chart.lookup(".chart-plot-background").setStyle("-fx-background-color: \"" + color + "\";");
    }
    
    // Approximate width of the tick on x axis
    private final int tickWidth = 14;
    
    double viewPortWidth = 600;
    double viewPortHeight = 400;

    Point2D dragPosition;

    private double currentZoomFactor = 0.1;
    private double zoomingSpeed = 3;
    double width;

    private double dragStrength = 0.0006;
    private double extraYdragStrength = 6;

    private LineChart chart;
}
