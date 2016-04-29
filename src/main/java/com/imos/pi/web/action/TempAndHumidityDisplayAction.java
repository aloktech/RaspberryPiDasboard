/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imos.pi.web.action;

import com.imos.pi.web.utils.DayLight;
import com.imos.pi.web.utils.HttpMethod;
import com.imos.pi.web.utils.RestClient;
import static com.imos.pi.web.utils.DashboardConstant.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LineChartModel;

/**
 *
 * @author Alok
 */
@ManagedBean(name = "displayAction")
@Setter
@Getter
public class TempAndHumidityDisplayAction implements Serializable {

    private LineChartModel lineModel;
    private Date startDate, endDate, todayDate;
    private UploadedFile file;
    private double maxTemp, minTemp, aveTemp, maxHumid, minHumid, aveHumid;
    private int interval;

    @PostConstruct
    public void init() {
        interval = 12;
        todayDate = new Date();
        createLineModels();
    }

    public void onSlideEnd(SlideEndEvent event) {
        FacesMessage message = new FacesMessage("Slide Ended", "Value: " + event.getValue());
        FacesContext.getCurrentInstance().addMessage(null, message);

        initCategoryModel(lineModel);
    }

    public void onDateSelect(SelectEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Date Selected", format.format(event.getObject())));

    }

    public void display() {
        initCategoryModel(lineModel);
    }

    public void upload() {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputstream()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            RestClient restClient = new RestClient();

            List<String> paths = new ArrayList<>();
            paths.add(BASIC);
            paths.add(ELASTIC);
            paths.add(UPLOAD);
            paths.add(TEMP_HUMID_DB);
            paths.add(TEMP_HUMID);

            restClient.setPaths(paths);
            restClient.setHttpMethod(HttpMethod.POST);
            restClient.setData(builder.toString());

            restClient.configure().setUrlPath().execute();

        } catch (IOException ex) {
            Logger.getLogger(TempAndHumidityDisplayAction.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void click() {
        RequestContext requestContext = RequestContext.getCurrentInstance();

        requestContext.update("form:display");
        requestContext.execute("PF('dlg').show()");
    }

    public LineChartModel getLineModel() {
        return lineModel;
    }

    private void createLineModels() {
        lineModel = new LineChartModel();
        lineModel.setTitle("Temperature and Humidity Chart");
        lineModel.setLegendPosition("n");
        lineModel.setShowPointLabels(false);
        lineModel.getAxes().put(AxisType.X, new CategoryAxis("Time"));
        Axis yAxis = lineModel.getAxis(AxisType.Y);
        yAxis.setLabel("Percentage");
        yAxis.setMin(20);
        yAxis.setMax(75);

        Axis xAxis = lineModel.getAxis(AxisType.X);
        xAxis.setTickAngle(90);
    }

    private LineChartModel initCategoryModel(LineChartModel model) {

        JSONObject inputData = new JSONObject();
        Calendar cal = GregorianCalendar.getInstance();

        inputData.put(START_TIME, extractTime(cal, startDate, DayLight.START));
        inputData.put(END_TIME, extractTime(cal, endDate, DayLight.END));

        RestClient restClient = new RestClient();

        List<String> paths = new ArrayList<>();
        paths.add(BASIC);
        paths.add(ELASTIC);
        paths.add(EXTRACT);
        restClient.setPaths(paths);

        restClient.setHttpMethod(HttpMethod.POST);
        restClient.setData(inputData.toString());

        String allData = restClient.configure().setUrlPath().execute();
        if (allData == null || allData.isEmpty()) {
            return model;
        }

        JSONObject tempData = new JSONObject(allData);
        JSONArray tempArray = tempData.getJSONObject(HITS).getJSONArray(HITS);
        ChartSeries temperature = new ChartSeries();
        temperature.setLabel(TEMPERATURE);
        ChartSeries humidity = new ChartSeries();
        humidity.setLabel(HUMIDITY);
        double tempd, humidd;
        int count = 0, valueCounter = 0;
        if (tempArray.length() > 0) {
            for (int index = 0; index < tempArray.length(); index++) {
                JSONObject tempJson = tempArray.getJSONObject(index).getJSONObject(SOURCE);
                String time = tempJson.getString(TIME);
                JSONObject data = tempJson.getJSONObject(DATA);

                tempd = data.getDouble(TEMP);
                humidd = data.getDouble(HUMID);

                if (interval > 0 && count % interval == 0) {
                    valueCounter = setTempAndHumidValue(tempd, humidd, temperature, time, humidity, valueCounter);
                } else if (interval == 0) {
                    valueCounter = setTempAndHumidValue(tempd, humidd, temperature, time, humidity, valueCounter);
                }
                count++;
            }
            aveTemp = Double.parseDouble(new DecimalFormat(DOUBLE_FORMAT).format(aveTemp / valueCounter));
            aveHumid = Double.parseDouble(new DecimalFormat(DOUBLE_FORMAT).format(aveHumid / valueCounter));
        } else {
            temperature.set(DATA_NOT_AVAILABLE, 50);
            humidity.set(DATA_NOT_AVAILABLE, 70);
            Axis xAxis = model.getAxis(AxisType.X);
            xAxis.setTickAngle(0);

            maxTemp = maxHumid = minTemp = minHumid = aveTemp = aveHumid = 0;
        }

        model.clear();
        model.addSeries(temperature);
        model.addSeries(humidity);

        return model;
    }

    private int setTempAndHumidValue(double tempd, double humidd, ChartSeries temperature, 
            String time, ChartSeries humidity, int valueCounter) {
        
        calculateMinMax(tempd, humidd);
        temperature.set(time, tempd);
        humidity.set(time, humidd);
        valueCounter++;
        return valueCounter;
    }

    private void calculateMinMax(double tempd, double humidd) {
        if (maxTemp < tempd) {
            maxTemp = tempd;
        }

        if (maxHumid < humidd) {
            maxHumid = humidd;
        }

        if (minTemp == 0) {
            minTemp = tempd;
        }
        if (minTemp > tempd) {
            minTemp = tempd;
        }

        if (minHumid == 0) {
            minHumid = humidd;
        }
        if (minHumid > humidd) {
            minHumid = humidd;
        }

        aveTemp += tempd;
        aveHumid += humidd;
    }

    private String extractTime(Calendar cal, Date date, DayLight dayLight) {
        StringBuilder builder = new StringBuilder();
        if (date == null) {
            date = new Date();
            cal.setTime(date);

            updateDate(builder, cal);

            if (dayLight == DayLight.START) {
                builder.append(String.valueOf(0));
                builder.append(UNDER_SCORE);
                builder.append(String.valueOf(0));
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                date.setTime(cal.getTimeInMillis());
                startDate = date;
            } else {
                builder.append(String.valueOf(23));
                builder.append(UNDER_SCORE);
                builder.append(String.valueOf(59));
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                date.setTime(cal.getTimeInMillis());
                endDate = date;
            }
        } else {
            cal.setTime(date);

            updateDate(builder, cal);

            builder.append(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
            builder.append(UNDER_SCORE);
            builder.append(String.valueOf(cal.get(Calendar.MINUTE)));
        }

        return builder.toString();
    }

    private void updateDate(StringBuilder builder, Calendar cal) {
        builder.append(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
        builder.append(UNDER_SCORE);
        builder.append(String.valueOf(cal.get(Calendar.MONTH) + 1));
        builder.append(UNDER_SCORE);
        builder.append(String.valueOf(cal.get(Calendar.YEAR)));
        builder.append(UNDER_SCORE);
    }
}
