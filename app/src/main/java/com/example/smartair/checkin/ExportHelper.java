package com.example.smartair.checkin;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExportHelper {

    private static final SimpleDateFormat FILE_DATE_FORMAT =
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    /************** PDF EXPORT — SAME LOCATION — JUST FORMATTED BETTER **************/
    public static void savePdfFromText(Context ctx, String text, String baseName) {

        if (Build.VERSION.SDK_INT < 29) {
            Toast.makeText(ctx, "PDF export requires Android 10+", Toast.LENGTH_LONG).show();
            return;
        }

        String filename = baseName + "_" + FILE_DATE_FORMAT.format(System.currentTimeMillis()) + ".pdf";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/");

        Uri uri = ctx.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(ctx, "Storage access blocked", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            PdfDocument pdf = new PdfDocument();
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdf.startPage(info);

            Canvas canvas = page.getCanvas();

            // ===== TITLE =====
            Paint title = new Paint();
            title.setTextSize(20f);
            title.setFakeBoldText(true);
            title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("SMART AIR — Symptom Report", 60, 60, title);

            // Section spacing start point
            int x = 60, y = 110, lineHeight = 22;

            // Body text formatting
            Paint body = new Paint();
            body.setTextSize(13f);

            // Draw content line-by-line — ONLY formatting changed
            for (String line : text.split("\n")) {
                if (y > 800) {
                    pdf.finishPage(page);
                    page = pdf.startPage(info);
                    canvas = page.getCanvas();
                    y = 60;
                }
                canvas.drawText(line, x, y, body);
                y += lineHeight;
            }

            pdf.finishPage(page);

            OutputStream out = ctx.getContentResolver().openOutputStream(uri);
            pdf.writeTo(out);
            out.close();
            pdf.close();

            Toast.makeText(ctx, "PDF saved to Downloads ✔", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(ctx, "PDF failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    /************** CSV EXPORT — SAME LOCATION — JUST FORMATTED BETTER **************/
    public static void saveCsv(Context ctx, List<Map<String, Object>> rows, String baseName) {

        if (rows == null || rows.isEmpty()) {
            Toast.makeText(ctx, "No history to export", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT < 29) {
            Toast.makeText(ctx, "CSV export requires Android 10+", Toast.LENGTH_LONG).show();
            return;
        }

        String filename = baseName + "_" + FILE_DATE_FORMAT.format(System.currentTimeMillis()) + ".csv";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/");

        Uri uri = ctx.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(ctx, "Storage access blocked", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // More readable CSV formatting only
            StringBuilder sb = new StringBuilder();
            sb.append("Date,Night Waking,Activity Limits,Cough/Wheeze,Chest Pain,Triggers,Submitted By\n");

            for (Map<String,Object> m : rows) {
                sb.append(quote(m.get("date"))).append(",");
                sb.append(quote(m.get("nightWaking"))).append(",");
                sb.append(quote(m.get("activityLimits"))).append(",");
                sb.append(quote(m.get("cough"))).append(",");
                sb.append(quote(m.get("chestPain"))).append(",");
                sb.append(quote(m.get("triggers"))).append(",");
                sb.append(quote(m.get("submittedBy"))).append("\n");
            }

            OutputStream out = ctx.getContentResolver().openOutputStream(uri);
            out.write(sb.toString().getBytes());
            out.close();

            Toast.makeText(ctx, "CSV saved to Downloads ✔", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(ctx, "CSV failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String quote(Object o) {
        if (o == null) return "\"\"";
        return "\"" + o.toString().replace("\"", "\"\"") + "\"";
    }
}
