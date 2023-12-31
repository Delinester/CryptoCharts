package CryptoCharts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainWindow extends Scene {
    public MainWindow() {
        super(new BorderPane(), windowWidth, windowHeight);
        rootLayout = (BorderPane) this.getRoot();
        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.TOP_CENTER);
        rootLayout.setCenter(centerVbox);

        // Initializes event handler
        ChartDrawerEventHandler chartDrawerEventHandler = new ChartDrawerEventHandler(this);

        Label windowLabel = new Label("Window: ");
        TextField frequencyField = new TextField();
        frequencyField.setText("full");
        frequencyField.setOnAction(chartDrawerEventHandler);

        Label freqLabel = new Label("Frequency: ");
        frequencyComboBox = new ComboBox<String>();
        ObservableList<String> freqList = FXCollections.observableArrayList(frequencies);
        frequencyComboBox.setItems(freqList);
        frequencyComboBox.setOnAction(chartDrawerEventHandler);
        frequencyComboBox.getSelectionModel().select(0);

        chartDrawerEventHandler.setFrequencyBox(frequencyComboBox);
        chartDrawerEventHandler.setWindowField(frequencyField);

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);
        hbox.getChildren().addAll(freqLabel, frequencyComboBox, windowLabel, frequencyField);
        rootLayout.setTop(hbox);

        rightVbox = new VBox();
        symbolsListView = new ListViewPanel("Symbols", DB_Manager.getInstance().getAvailableSymbols());
        symbolsListView.setMaxHeight(windowHeight / 2);
        rightVbox.getChildren().add(symbolsListView);
        chartDrawerEventHandler.setSymbolsList(symbolsListView.getListView());
        symbolsListView.getListView().setOnMouseClicked(chartDrawerEventHandler);

        indicatorsListView = new ListViewPanel("Indicators", Indicator.indicators);
        indicatorsListView.setMaxHeight(windowHeight / 3);
        rightVbox.getChildren().add(indicatorsListView);

        rightVbox.setSpacing(30);

        rootLayout.setRight(rightVbox);

        leftVbox = new VBox();
        rootLayout.setLeft(leftVbox);
        leftVbox.setSpacing(15);

        ChartMoveHandler.setCenterBox(centerVbox);
        ChartMoveHandler.setLeftBox(leftVbox);
        ChartMoveHandler.setLayouts(centerVbox, leftVbox);

        IndicatorConfigMenu.setMainWindowRef(this);
    }

    public static int getWindowWidth() {
        return windowWidth;
    }

    public static int getWindowHeight() {
        return windowHeight;
    }

    public void constructChart(String symbol, String frequency, String windowString) {        
        cleanMainChart();
        // Download the data from website
        String fileName = "Binance_" + symbol + "_" + frequency + ".csv";
        String path = "src\\CryptoCharts\\Charts\\" + fileName;
        try {
            ReadCSV.download("https://www.cryptodatadownload.com/cdd/" + fileName, path);
        } catch (IOException e) {
            System.out.println("FILE WRITING ERROR: " + e.getMessage());
            return;
        }
        ReadCSV csv = new ReadCSV("src\\CryptoCharts\\Charts\\" + fileName, ",", 0);

        // Retrieve values from the csv
        datesVector = csv.getAllColumnValues("Date");
        closePriceVector = csv.getColumnAsFloat("Close");
        openPriceVector = csv.getColumnAsFloat("Open");
        highPriceVector = csv.getColumnAsFloat("High");
        lowPriceVector = csv.getColumnAsFloat("Low");

        int numberOfEntries = datesVector.size();
        if (windowString.equals("full"))
            window = datesVector.size();
        else
            window = Integer.parseInt(windowString);

        if (window < 1)
            return;

        Collections.reverse(datesVector);
        Collections.reverse(closePriceVector);

        ObservableList<Float> closePrices = FXCollections.observableArrayList();
        for (int i = numberOfEntries - window; i < numberOfEntries; i++)
            closePrices.add(closePriceVector.get(i));

        ObservableList<String> dates = FXCollections.observableArrayList();
        for (int i = numberOfEntries - window; i < numberOfEntries; i++)
            dates.add(datesVector.get(i));

        float maxPrice = Collections.max(closePrices);
        float minPrice = Collections.min(closePrices);

        NumberAxis yAxis = new NumberAxis("price",
                minPrice - minPrice / 100, maxPrice + maxPrice / 100, Collections.max(closePriceVector) / 100);

        CategoryAxis xAxis = new CategoryAxis(dates);

        XYChart.Series<String, Float> series = new XYChart.Series<String, Float>();
        for (int i = 0; i < window; i++) {
            float close = closePrices.get(i);
            String closeStr = Float.toString(close);
            String open = Float.toString(openPriceVector.get(window - 1 - i));
            String high = Float.toString(highPriceVector.get(window - 1 - i));
            String low = Float.toString(lowPriceVector.get(window - 1 - i));
            String date = dates.get(i);
            XYChart.Data<String, Float> data = new XYChart.Data<String, Float>(date, close);
            data.setNode(new ChartHoverInfo("Date: ", date, "Close: ", closeStr, "Open: ", open, "High: ", high,
                    "Low: ", low));
            series.getData().add(data);
        }

        ConfigurableChart configurableChart = new ConfigurableChart(symbol, xAxis, yAxis, series,
                window, windowWidth / 2, windowHeight / 2);
        charts.add(configurableChart);
        centerVbox.getChildren().add(configurableChart);
    }

    public void constructIndicatorChart(Indicator panel) {

        ConfigurableChart chart = panel.getChart(closePriceVector, datesVector, window);
        charts.add(chart);
        centerVbox.getChildren().addAll(chart);
    }

    private void cleanMainChart() {
        centerVbox.getChildren().removeAll(charts);
        leftVbox.getChildren().removeAll(charts);
        charts.clear();
    }

    private final static int windowWidth = 1200;
    private final static int windowHeight = 800;
    private BorderPane rootLayout;

    private VBox centerVbox;
    private VBox rightVbox;
    private VBox leftVbox;

    private ArrayList<ConfigurableChart> charts = new ArrayList<ConfigurableChart>();

    private ListViewPanel symbolsListView;
    private ListViewPanel indicatorsListView;

    private ComboBox<String> frequencyComboBox;
    private final String[] frequencies = { "d", "1h" };

    private Vector<String> datesVector;
    private Vector<Float> closePriceVector;
    private Vector<Float> openPriceVector;
    private Vector<Float> highPriceVector;
    private Vector<Float> lowPriceVector;
    private int window;
}
