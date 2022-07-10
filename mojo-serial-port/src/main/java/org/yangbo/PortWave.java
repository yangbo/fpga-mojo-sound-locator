package org.yangbo;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.DataSetError;
import de.gsi.dataset.spi.DefaultErrorDataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import de.gsi.dataset.spi.DoubleErrorDataSet;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PortWave extends VBox {
    private DefaultNumericAxis xAxis = new DefaultNumericAxis();
    private DefaultNumericAxis yAxis = new DefaultNumericAxis();
    private XYChart chart = new XYChart(xAxis, yAxis);
    private DefaultErrorDataSet dataSet = new DefaultErrorDataSet("Mojo串口数据");

    /**
     * Y轴最大刻度，固定 Y 轴范围用
     */
    private int maxY = 255;

    public PortWave(int maxY) {
        this.maxY = maxY;
        getChildren().add(chart);
        setVgrow(chart, Priority.ALWAYS);
        // 固定Y轴高度
        yAxis.set(-this.maxY, this.maxY);
        yAxis.setAutoRanging(false);
        chart.getDatasets().addAll(dataSet);
        chart.setLegendVisible(false);
        // 设置 render，去掉折线中的点
        ErrorDataSetRenderer errorDataSetRenderer = new ErrorDataSetRenderer();
        initErrorDataSetRenderer(errorDataSetRenderer);

        chart.getRenderers().clear();
        chart.getRenderers().add(errorDataSetRenderer);
    }

    protected void initErrorDataSetRenderer(final ErrorDataSetRenderer eRenderer) {
        eRenderer.setErrorType(ErrorStyle.ERRORSURFACE);
        eRenderer.setDashSize(0); // plot pixel-to-pixel distance
        eRenderer.setDrawMarker(false);
        final DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) eRenderer.getRendererDataReducer();
        reductionAlgorithm.setMinPointPixelDistance(0);
    }

    public XYChart getChart() {
        return chart;
    }

    public DefaultErrorDataSet getDataSet() {
        return dataSet;
    }

}
