package com.equalinformation.dashboardcollections.ui;

import com.equalinformation.dashboardcollections.applications.bam.EsperStatementsCreator;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.github.wolfie.refresher.Refresher;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import org.apache.log4j.Logger;
import org.vaadin.vaadinvisualizations.Gauge;

import javax.servlet.annotation.WebServlet;
import java.text.DecimalFormat;

/**
 * Created by bpupadhyaya on 1/17/16.
 */
@Theme("mytheme")
@Widgetset("com.equalinformation.dashboardcollections.MyAppWidgetset")
public class DashboardUI extends UI {

    @WebServlet(urlPatterns = "/*", name = "DashboardUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = DashboardUI.class, productionMode = false)
    public static class DashboardUIServlet extends VaadinServlet {
    }    private static final Logger log = Logger.getLogger(DashboardUI.class);

    private static final DecimalFormat avgRequestedAmountformatter = new DecimalFormat("##0.0");
    private static final DecimalFormat avgProcessDurationformatter = new DecimalFormat("###0");

    private UpdateListener requestedAmountListener;
    private UpdateListener loanedAmountListener;
    private UpdateListener processDurationListener;

    Panel loanInfoPanel;
    Panel processInfoPanel;

    private Label avgRequestedAmountLabel = new Label();
    private Label maxRequestedAmountLabel = new Label();
    private Label sumRequestedAmountLabel = new Label();
    private Label numLoansLabel = new Label();
    private Label sumLoanedAmountLabel = new Label();
    private Label avgProcessDurationLabel = new Label();
    private Label maxProcessDurationLabel = new Label();



    protected void init(VaadinRequest vaadinRequest) {
        log.info("Starting BAM application");
        GridLayout mainWindowLayout = new GridLayout(2,3);

        final VerticalLayout layout = new VerticalLayout();

        mainWindowLayout.setSizeFull();
        mainWindowLayout.setMargin(true);
        // mainWindowLayout.setSpacing(true);

        // Add the window refresher component
        Refresher refresher = new Refresher();
        refresher.setRefreshInterval(1000);
        mainWindowLayout.addComponent(refresher,0,0);

        // Panel containing 2 gauges with loan information.
        loanInfoPanel = new Panel("Number of loans + Sum of loaned amount");
        loanInfoPanel.setHeight("170px");
        loanInfoPanel.setWidth("300px");
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        loanInfoPanel.setContent(horizontalLayout);
        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setSizeFull();
        mainWindowLayout.addComponent(loanInfoPanel,0,1);

        // Panel containing 2 gauges with process information.
        processInfoPanel = new Panel("Average + Maximum process duration");
        processInfoPanel.setHeight("170px");
        processInfoPanel.setWidth("300px");
        horizontalLayout = new HorizontalLayout();
        processInfoPanel.setContent(horizontalLayout);
        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setSizeFull();
        mainWindowLayout.addComponent(processInfoPanel,0,2);

        // Panel containing a textual representation
        Panel textualRepresentationPanel = new Panel("Detailed information");
        // textualRepresentationPanel.setHeight("90%");
        textualRepresentationPanel.setWidth("300px");
        GridLayout gridLayout = new GridLayout(3, 7);
        textualRepresentationPanel.setContent(gridLayout);
        gridLayout.setMargin(true);
        gridLayout.setSpacing(true);
        addLabelAndValue(gridLayout, "Average requested amount", avgRequestedAmountLabel,"");
        addLabelAndValue(gridLayout, "Maximum requested amount", maxRequestedAmountLabel, "");
        addLabelAndValue(gridLayout, "Sum of requested amount", sumRequestedAmountLabel, "");
        addLabelAndValue(gridLayout, "Number of loans", numLoansLabel, "");
        addLabelAndValue(gridLayout, "Sum of loaned amount", sumLoanedAmountLabel, "");
        addLabelAndValue(gridLayout, "Average process duration", avgProcessDurationLabel, "ms.");
        addLabelAndValue(gridLayout, "Maximum process duration", maxProcessDurationLabel, "ms.");
        mainWindowLayout.addComponent(textualRepresentationPanel,1,1,1,2);

        // Monitor requested amount
        requestedAmountListener = new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                Double  avgRequestedAmount = (Double) newEvents[0].get("avgRequestedAmount");
                Integer maxRequestedAmount = (Integer)newEvents[0].get("maxRequestedAmount");
                Integer sumRequestedAmount = (Integer)newEvents[0].get("sumRequestedAmount");

                avgRequestedAmountLabel.setValue((avgRequestedAmount!=null) ? avgRequestedAmountformatter.format(avgRequestedAmount) : "");
                maxRequestedAmountLabel.setValue(String.valueOf(maxRequestedAmount));
                sumRequestedAmountLabel.setValue(String.valueOf(sumRequestedAmount));
            }
        };
        EPAdministrator epAdmin = EPServiceProviderManager.getDefaultProvider().getEPAdministrator();
        epAdmin.getStatement(EsperStatementsCreator.REQUESTED_AMOUNT_STATEMENT_NAME).addListenerWithReplay(requestedAmountListener);

        // Monitor loans
        loanedAmountListener = new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                Long numLoans = (Long) newEvents[0].get("numLoans");
                Integer sumLoanedAmount = (Integer) newEvents[0].get("sumLoanedAmount");

//                numLoansLabel.setValue(numLoans);
//                sumLoanedAmountLabel.setValue(sumLoanedAmount);

//                loanInfoPanel.removeAllComponents();

                Gauge numLoansGauge= new Gauge();
                numLoansGauge.setOption("max", 20);
                numLoansGauge.setOption("redFrom", 0);
                numLoansGauge.setOption("redTo", 3);
                numLoansGauge.setOption("yellowFrom", 3);
                numLoansGauge.setOption("yellowTo", 6);
                numLoansGauge.setOption("minorTicks",5);
                numLoansGauge.setSizeFull();
                numLoansGauge.add("#", numLoans);

                Gauge sumLoanedAmountGauge= new Gauge();
                sumLoanedAmountGauge.setOption("max", 800);
                sumLoanedAmountGauge.setOption("redFrom", 0);
                sumLoanedAmountGauge.setOption("redTo", 150);
                sumLoanedAmountGauge.setOption("yellowFrom", 150);
                sumLoanedAmountGauge.setOption("yellowTo", 300);
                sumLoanedAmountGauge.setOption("minorTicks",4);
                sumLoanedAmountGauge.setSizeFull();
                sumLoanedAmountGauge.add("", (sumLoanedAmount!=null)?sumLoanedAmount:0);

//                loanInfoPanel.addComponent(numLoansGauge);
//                loanInfoPanel.addComponent(sumLoanedAmountGauge);
                layout.addComponent(numLoansGauge);
                layout.addComponent(sumLoanedAmountGauge);
            }
        };
        epAdmin.getStatement(EsperStatementsCreator.LOANED_AMOUNT_STATEMENT_NAME).addListenerWithReplay(loanedAmountListener);

        // Monitor process duration
        processDurationListener = new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                Double avgProcessDuration = (Double) newEvents[0].get("avgProcessDuration");
                Long maxProcessDuration = (Long) newEvents[0].get("maxProcessDuration");

                avgProcessDurationLabel.setValue((avgProcessDuration!=null) ? avgProcessDurationformatter.format(avgProcessDuration) : "");
//                maxProcessDurationLabel.setValue(maxProcessDuration);

//                processInfoPanel.removeAllComponents();

                Gauge avgProcessDurationGauge= new Gauge();
                avgProcessDurationGauge.setOption("max", 2000);
                avgProcessDurationGauge.setOption("yellowFrom", 1600);
                avgProcessDurationGauge.setOption("yellowTo", 1800);
                avgProcessDurationGauge.setOption("redFrom", 1800);
                avgProcessDurationGauge.setOption("redTo", 2000);
                avgProcessDurationGauge.setOption("minorTicks",5);
                avgProcessDurationGauge.setSizeFull();
                avgProcessDurationGauge.add("ms.", (avgProcessDuration!=null) ? avgProcessDuration.intValue() : 0);

                Gauge maxProcessDurationGauge= new Gauge();
                maxProcessDurationGauge.setOption("max",2800);
                maxProcessDurationGauge.setOption("yellowFrom", 1600);
                maxProcessDurationGauge.setOption("yellowTo", 1800);
                maxProcessDurationGauge.setOption("redFrom", 1800);
                maxProcessDurationGauge.setOption("redTo", 2800);
                maxProcessDurationGauge.setOption("minorTicks",7);
                maxProcessDurationGauge.setSizeFull();
                maxProcessDurationGauge.add("ms.", (maxProcessDuration!=null) ? maxProcessDuration : 0);

//                processInfoPanel.addComponent(avgProcessDurationGauge);
//                processInfoPanel.addComponent(maxProcessDurationGauge);
                layout.addComponent(avgProcessDurationGauge);
                layout.addComponent(maxProcessDurationGauge);
            }
        };
        epAdmin.getStatement(EsperStatementsCreator.PROCESS_DURATION_STATEMENT_NAME).addListenerWithReplay(	processDurationListener);

//        layout.addComponents(name, button);
        layout.setMargin(true);
        layout.setSpacing(true);


        setContent(layout);
    }


    private void addLabelAndValue(GridLayout layout, String labelText, Component value, String unitText) {
        Label label = new Label(labelText + ":");
        layout.addComponent(label);
        layout.setComponentAlignment(label, Alignment.MIDDLE_RIGHT);
        layout.addComponent(value);
        layout.setComponentAlignment(value, Alignment.MIDDLE_RIGHT);
        label = new Label(unitText);
        layout.addComponent(label);
        layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
    }

//    @Override
//    protected void init(VaadinRequest vaadinRequest) {
//        final VerticalLayout layout = new VerticalLayout();
//
//        final TextField name = new TextField();
//        name.setCaption("Type your name here:");
//
//        Button button = new Button("Click Me");
//        button.addClickListener(e -> {
//            layout.addComponent(new Label("Thanks " + name.getValue()
//                    + ", it works!"));
//        });
//
//        layout.addComponents(name, button);
//        layout.setMargin(true);
//        layout.setSpacing(true);
//
//        setContent(layout);
//    }


}