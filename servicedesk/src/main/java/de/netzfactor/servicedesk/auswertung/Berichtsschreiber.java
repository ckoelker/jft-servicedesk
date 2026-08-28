package de.netzfactor.servicedesk.auswertung;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Drei Ausgabeformate fuer jeden Bericht - ohne dass diese Klasse einen davon
 * kennt.
 *
 * <p>Was ein record ist und wie seine Spalten heissen, steht in den
 * {@link Spalte}-Annotationen; gelesen wird es zur Laufzeit ueber
 * {@link RecordComponent}. Kommt in {@link Zeilen} ein vierter Bericht dazu,
 * aendert sich hier keine Zeile.
 */
public final class Berichtsschreiber {

    private Berichtsschreiber() {
    }

    /** Die Ueberschriften in der Reihenfolge der record-Komponenten. */
    public static List<String> kopf(Class<?> art) {
        return Arrays.stream(komponenten(art))
                .map(komponente -> Optional.ofNullable(komponente.getAnnotation(Spalte.class))
                        .map(Spalte::value)
                        .orElse(komponente.getName()))
                .toList();
    }

    /** Die Werte je Datensatz, Spalte fuer Spalte in derselben Reihenfolge. */
    public static List<List<Object>> zeilen(List<?> daten) {
        if (daten.isEmpty()) {
            return List.of();
        }
        RecordComponent[] komponenten = komponenten(daten.get(0).getClass());
        List<List<Object>> alle = new ArrayList<>();
        for (Object datensatz : daten) {
            List<Object> zeile = new ArrayList<>();
            for (RecordComponent komponente : komponenten) {
                zeile.add(wert(komponente, datensatz));
            }
            alle.add(zeile);
        }
        return alle;
    }

    public static String alsText(String titel, List<?> daten) {
        StringBuilder text = new StringBuilder(titel).append(System.lineSeparator());
        if (daten.isEmpty()) {
            return text.append("(keine Daten)").toString();
        }

        List<String> kopf = kopf(daten.get(0).getClass());
        List<List<Object>> zeilen = zeilen(daten);
        int[] breiten = breiten(kopf, zeilen);

        text.append(zeile(kopf.toArray(), breiten, zeilen.get(0)));
        text.append(System.lineSeparator());
        for (int spalte = 0; spalte < breiten.length; spalte++) {
            text.append(spalte == 0 ? "" : "  ").append("-".repeat(breiten[spalte]));
        }
        for (List<Object> werte : zeilen) {
            text.append(System.lineSeparator()).append(zeile(werte.toArray(), breiten, zeilen.get(0)));
        }
        return text.toString();
    }

    public static byte[] alsExcel(String titel, List<?> daten) {
        try (XSSFWorkbook mappe = new XSSFWorkbook();
             ByteArrayOutputStream strom = new ByteArrayOutputStream()) {

            XSSFSheet blatt = mappe.createSheet(titel);
            XSSFFont fett = mappe.createFont();
            fett.setBold(true);
            XSSFCellStyle kopfstil = mappe.createCellStyle();
            kopfstil.setFont(fett);

            List<String> kopf = daten.isEmpty() ? List.of() : kopf(daten.get(0).getClass());
            XSSFRow kopfzeile = blatt.createRow(0);
            for (int spalte = 0; spalte < kopf.size(); spalte++) {
                XSSFCell zelle = kopfzeile.createCell(spalte);
                zelle.setCellValue(kopf.get(spalte));
                zelle.setCellStyle(kopfstil);
            }

            List<List<Object>> zeilen = zeilen(daten);
            for (int nummer = 0; nummer < zeilen.size(); nummer++) {
                XSSFRow zeile = blatt.createRow(nummer + 1);
                List<Object> werte = zeilen.get(nummer);
                for (int spalte = 0; spalte < werte.size(); spalte++) {
                    fuelle(zeile.createCell(spalte), werte.get(spalte));
                }
            }
            for (int spalte = 0; spalte < kopf.size(); spalte++) {
                blatt.autoSizeColumn(spalte);
            }

            mappe.write(strom);
            return strom.toByteArray();
        } catch (IOException fehler) {
            throw new IllegalStateException("Excel-Bericht '" + titel + "' fehlgeschlagen", fehler);
        }
    }

    public static byte[] alsPdf(String titel, List<?> daten) {
        ByteArrayOutputStream strom = new ByteArrayOutputStream();
        Document dokument = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(dokument, strom);
            dokument.open();
            dokument.add(new Paragraph(titel, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            dokument.add(new Paragraph(" "));

            if (!daten.isEmpty()) {
                Font fett = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
                List<String> kopf = kopf(daten.get(0).getClass());

                PdfPTable tabelle = new PdfPTable(kopf.size());
                tabelle.setWidthPercentage(100);
                for (String ueberschrift : kopf) {
                    tabelle.addCell(new PdfPCell(new Phrase(ueberschrift, fett)));
                }
                for (List<Object> zeile : zeilen(daten)) {
                    for (Object wert : zeile) {
                        tabelle.addCell(new PdfPCell(new Phrase(text(wert), normal)));
                    }
                }
                dokument.add(tabelle);
            }
            dokument.close();
            return strom.toByteArray();
        } catch (DocumentException fehler) {
            throw new IllegalStateException("PDF-Bericht '" + titel + "' fehlgeschlagen", fehler);
        }
    }

    private static RecordComponent[] komponenten(Class<?> art) {
        RecordComponent[] komponenten = art.getRecordComponents();
        if (komponenten == null) {
            throw new IllegalArgumentException(art.getSimpleName() + " ist kein record");
        }
        return komponenten;
    }

    private static Object wert(RecordComponent komponente, Object datensatz) {
        try {
            return komponente.getAccessor().invoke(datensatz);
        } catch (IllegalAccessException | InvocationTargetException fehler) {
            throw new IllegalStateException("Spalte '" + komponente.getName() + "' von "
                    + datensatz.getClass().getSimpleName() + " liess sich nicht lesen", fehler);
        }
    }

    private static int[] breiten(List<String> kopf, List<List<Object>> zeilen) {
        int[] breiten = new int[kopf.size()];
        for (int spalte = 0; spalte < kopf.size(); spalte++) {
            breiten[spalte] = kopf.get(spalte).length();
        }
        for (List<Object> werte : zeilen) {
            for (int spalte = 0; spalte < breiten.length; spalte++) {
                breiten[spalte] = Math.max(breiten[spalte], text(werte.get(spalte)).length());
            }
        }
        return breiten;
    }

    private static String zeile(Object[] werte, int[] breiten, List<Object> muster) {
        StringBuilder zeile = new StringBuilder();
        for (int spalte = 0; spalte < breiten.length; spalte++) {
            // Zahlen rechts, alles andere links - die Art der Spalte verraet die erste Datenzeile.
            String ausrichtung = muster.get(spalte) instanceof Number ? "%" : "%-";
            zeile.append(spalte == 0 ? "" : "  ")
                 .append(String.format(ausrichtung + breiten[spalte] + "s", text(werte[spalte])));
        }
        return zeile.toString().stripTrailing();
    }

    private static void fuelle(XSSFCell zelle, Object wert) {
        // Zahlen als Zahlen: sonst steht in Excel ein Text, mit dem sich nicht rechnen laesst.
        if (wert instanceof Number zahl) {
            zelle.setCellValue(zahl.doubleValue());
        } else if (wert != null) {
            zelle.setCellValue(String.valueOf(wert));
        }
    }

    private static String text(Object wert) {
        if (wert == null) {
            return "";
        }
        if (wert instanceof Double zahl) {
            return String.format(Locale.GERMANY, "%.1f", zahl);
        }
        return String.valueOf(wert);
    }
}
