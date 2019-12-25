package com.probe.aki.tuulikello;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;

import com.probe.aki.tuulikello.datasource.WeatherData;
import com.probe.aki.tuulikello.datasource.Fmi;
import com.probe.aki.tuulikello.datasource.WindGuru;

public class SimpleWindSpeedView extends View {

    private static final int MARGINALSIZE = 10;
    private static final int MARGINALSIZE2 = 20;
    private static final int RELOADTIME = 5;

    private static final long HOUR3 = 3600*3000;
    private static final int STATIONSCOLOR[] = {Color.RED,Color.GREEN,Color.BLUE,Color.CYAN};

    private Paint paint;
    private int sizex;
    private int sizey;
    private int sizeseparate;
    private Context context;

    private ArrayList<WeatherData> observations = new ArrayList<WeatherData>();
    private WeatherData forecast = null;

    public SimpleWindSpeedView(Context context) {
        super(context);
        this.context = context;
        paint = new Paint();
    }

    public SimpleWindSpeedView(Context context, AttributeSet set) {
        super(context, set);
        this.context = context;
        paint = new Paint();
    }

    public void loadNewValues(String observations_str,String forecast_str){
        if(observations_str !=null && forecast_str != null && ((observations.size() == 0) || (observations.size() > 0 && !observations.get(0).isUpdated(RELOADTIME))))
            new WeatherWebServiceTask().execute(observations_str,forecast_str);
    }

    public void loadNewValuesForce(String observations_str,String forecast_str){
        if(observations_str !=null && forecast_str != null)
            new WeatherWebServiceTask().execute(observations_str,forecast_str);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        sizex = this.getWidth() - MARGINALSIZE2;
        sizey = this.getHeight() - MARGINALSIZE2;
        sizeseparate = sizex - sizex/6;

        if(forecast != null && observations.size() > 0) {
            drawNormal(canvas);
        }
    }

    private void drawNormal(Canvas g){
        int loop;
        double max_tmp = -1;
        double max = 0;

        paint.setColor(Color.YELLOW);
        g.drawLine((sizeseparate + MARGINALSIZE),MARGINALSIZE,(sizeseparate + MARGINALSIZE),sizey,paint);
        g.drawText(Integer.toString(forecast.getStep()[0].getHours()), (sizeseparate + MARGINALSIZE), sizey+15,paint);
        g.drawText(Integer.toString(forecast.getStep()[1].getHours()), sizex, sizey+15,paint);

        Date now = new Date();
        Date historyDate = new Date(now.getTime() - HOUR3);

        Calendar timestep = Calendar.getInstance();
        double kerroin = (double) (sizex-sizex/6) / 3;
        timestep.setTime(historyDate);
        timestep.add(Calendar.HOUR_OF_DAY, 1);
        int move = (int)(kerroin - ((double)timestep.get(Calendar.MINUTE)/60.0) * kerroin);

        for (loop = 0; loop < 3; loop++) {
            int x = (int) ((double) loop * kerroin) + move + MARGINALSIZE;
            paint.setColor(Color.GRAY);
            g.drawLine(x, sizey - 10, x, sizey, paint);
            paint.setColor(Color.WHITE);
            g.drawText("" + timestep.get(Calendar.HOUR_OF_DAY), x, sizey + 15, paint);
            timestep.add(Calendar.HOUR_OF_DAY, 1);
        }

        for (WeatherData observationstation : observations) {
            for (double speed : observationstation.getWindspeed()) {
                if (max_tmp < speed)
                    max_tmp = speed;
            }
            if (max_tmp < forecast.getWindspeed()[0])
                max_tmp = forecast.getWindspeed()[0];
            if (max_tmp < forecast.getWindspeed()[1])
                max_tmp = forecast.getWindspeed()[1];
        }

        while(max_tmp > max)
            max += 5;
        double ty = (double)(sizey - MARGINALSIZE)/5.0;
        for(loop=0;loop<6;loop++){
            int y = sizey-MARGINALSIZE - (int)((double)loop * ty) + MARGINALSIZE;
            paint.setColor(Color.GRAY);
            g.drawLine(MARGINALSIZE,y,(sizex+MARGINALSIZE),y,paint);
            paint.setColor(Color.WHITE);
            int ms = (int)(max / 5.0 * loop);
            g.drawText(String.valueOf(ms), 2, y,paint);
        }
        int count = 0;
        for (WeatherData observationstation : observations)
            drewFigure(g,historyDate,now,observationstation.getStep(),observationstation.getWindspeed(),max,STATIONSCOLOR[count++]);
        drewForecast(g, forecast.getWindspeed(), max);

        int y = sizey - sizey/5;
        paint.setColor(Color.WHITE);
        drewAngle(g, observations.get(0).getWinddirection()[observations.get(0).getWinddirection().length-1], sizex-sizex/5, y);
        drewAngle(g, forecast.getWinddirection()[0]  , sizex-sizex/12, y);
    }

    private void drewFigure(Canvas canvas, Date start_point, Date end_point, Date[] destination, double[] wave, double max, int color) {
        double time_length = (double)(end_point.getTime() - start_point.getTime()) / (double)sizeseparate;
        float xyw[] = new float[(destination.length - 1) * 4];
        int l2 = 0;
        paint.setColor(color);
        for (int loop = 0; loop < destination.length - 1; loop++) {
            xyw[l2++] = (int) (((double) (destination[loop].getTime() - start_point.getTime()) / time_length)) + MARGINALSIZE;
            xyw[l2++] = sizey - (int) ((double) (sizey - MARGINALSIZE) * wave[loop] / max);
            xyw[l2++] = (int) (((double) (destination[loop + 1].getTime() - start_point.getTime()) / time_length)) + MARGINALSIZE;
            xyw[l2++] = sizey - (int) ((double) (sizey - MARGINALSIZE) * wave[loop + 1] / max);
        }
        canvas.drawLines(xyw, 0, xyw.length, paint);
    }

    private void drewForecast(Canvas g, double wave[], double max){
        paint.setColor(Color.WHITE);
        g.drawLine((sizeseparate + MARGINALSIZE  ), (sizey - (int) ((double) (sizey - MARGINALSIZE) * wave[0] / max)),sizex + MARGINALSIZE,
                (sizey - (int) ((double) (sizey - MARGINALSIZE) * wave[1] / max)),paint);
    }

    private void drewAngle(Canvas canvas, double winddirection, int tmpx, int tmpy){
        int yc = (int) (10.0 * Math.sin(winddirection * Math.PI / 180.0));
        int xc = (int) (10.0 * Math.cos(winddirection * Math.PI / 180.0));
        canvas.drawLine(tmpx + xc, tmpy + yc, tmpx - xc, tmpy - yc, paint);
        int yc1 = (int) (6.0 * Math.sin((winddirection + 45.0) * Math.PI / 180.0));
        int xc1 = (int) (6.0 * Math.cos((winddirection + 45.0) * Math.PI / 180.0));
        int yc2 = (int) (6.0 * Math.sin((winddirection - 45.0) * Math.PI / 180.0));
        int xc2 = (int) (6.0 * Math.cos((winddirection - 45.0) * Math.PI / 180.0));
        canvas.drawLine(tmpx + xc, tmpy + yc, tmpx + xc1, tmpy + yc1, paint);
        canvas.drawLine(tmpx + xc, tmpy + yc, tmpx + xc2, tmpy + yc2, paint);
    }

    private class WeatherWebServiceTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute()
        {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMax(100);
            progressDialog.setMessage(getResources().getString(R.string.download_text));
            progressDialog.setTitle(getResources().getString(R.string.app_name));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        protected String doInBackground(String... urls) {
            return readXMLdata(urls[0],urls[1]);
        }

        protected void onPostExecute(String result) {
            try {
                if (progressDialog != null)
                    progressDialog.dismiss();
                if (result.equals("OK"))
                    invalidate();
                else
                    Toast.makeText(context, R.string.error_download_text, Toast.LENGTH_SHORT).show();
            }catch (Exception e){
            }
        }

        private String readXMLdata(String observation_str,String forecast_str) {
            try {

                Date now = new Date();
                Date historyDate = new Date(now.getTime() - HOUR3);
                Date futureDate = new Date(now.getTime() + HOUR3);

                SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00'Z'");
                sim.setTimeZone(TimeZone.getTimeZone("GMT"));
                int prosent = 0;
                observations.clear();
                String[] row = observation_str.split("\n");
                int prosent_add = 100 / row.length;
                for (int i = 0; i < row.length; i++) {
                    String column[] = row[i].split(";");
                    if(column[1].equals("0")){
                        observations.add(new Fmi("http://opendata.fmi.fi/wfs?request=getFeature&storedquery_id=fmi::observations::weather::timevaluepair&fmisid="
                                + column[0] + "&starttime=" + sim.format(historyDate) + "&endtime=" + sim.format(now) + "&parameters=windspeedms,WindDirection"));
                    } else {
                        observations.add(new WindGuru("https://www.windguru.cz/int/wgsapi.php?id_station="+column[0]+"&password="+column[2]
                                +"&q=station_data_last&hours=3&vars=wind_avg,wind_direction"));
                    }
                    progressDialog.incrementProgressBy(prosent += prosent_add);
                }
                forecast = new Fmi("http://opendata.fmi.fi/wfs?request=getFeature&storedquery_id=fmi::forecast::hirlam::surface::point::timevaluepair&place="+forecast_str
                        + "&starttime="+sim.format(now)+"&endtime="+sim.format(futureDate)+"&parameters=windspeedms,WindDirection");

            } catch (Exception e) {
                e.printStackTrace();
                return "FALSE";
            }
            return "OK";
        }
    }
}