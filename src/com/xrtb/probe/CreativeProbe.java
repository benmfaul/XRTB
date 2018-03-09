package com.xrtb.probe;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.atomic.LongAdder;

/**
 * A class that keeps up with the creative probes (why the creative didn't bid).
 *
 * @author Ben M. Faul
 */
public class CreativeProbe {

    String creative;
    Map<String, LongAdder> probes;
    LongAdder total = new LongAdder();
    LongAdder bid = new LongAdder();

    public static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public CreativeProbe() {

    }

    public CreativeProbe(String creative) {
        this.creative = creative;
        probes = new HashMap();
    }

    public void reset() {
        probes = new HashMap();
        total = new LongAdder();
        bid = new LongAdder();

    }

    public void process(String key) {
        LongAdder ad = probes.get(key);
        if (ad == null) {
            ad = new LongAdder();
            probes.put(key, ad);
        }
        ad.increment();
        total.increment();
    }

    public void process() {
        total.increment();
        bid.increment();
    }

    public String report() {
        StringBuilder report = new StringBuilder();
        report.append("\t\t\ttotal = ");
        report.append(total.sum());
        report.append(", bids = ");
        report.append("\n");
        for (Map.Entry<String, LongAdder> entry : probes.entrySet()) {
            String key = entry.getKey();
            report.append("\t\t\t");
            report.append(key);
            report.append(" = ");
            LongAdder ad = entry.getValue();
            report.append(ad.sum());
            report.append(",   (");
            double v = total.sum();
            double vx = ad.sum();
            report.append(100 * vx / v);
            report.append(")\n");
        }

        return report.toString();
    }

    public void reportCsv(StringBuilder sb, String pre) {

        pre = pre + creative + "," + total.sum() + ", " + bid.sum();

        // Sort the list
        List<EntryField> values = new ArrayList();
        for (Map.Entry<String, LongAdder> entry : probes.entrySet()) {
            EntryField ef = new EntryField(entry.getKey(), entry.getValue().sum());
            values.add(ef);
        }
        Collections.sort(values, Collections.reverseOrder());

        for (EntryField ef : values) {
            String key = "\"" + ef.key.trim() + "\"";
            sb.append(pre + "," + key + "," + total.sum() + "," + ef.value + "\n");
        }

    }

    public void reportJson(StringBuilder sb, long grandtotal, String exchange, long bidstotal, String campaign) throws Exception {

        for (Map.Entry<String, LongAdder> entry : probes.entrySet()) {
            EntryField ef = new EntryField(entry.getKey(), entry.getValue().sum());

            Map report = new HashMap();
            report.put("timestamp",System.currentTimeMillis());
            report.put("exchange",exchange);
            report.put("campaign",campaign);
            report.put("creative",creative);
            report.put("reason",ef.key.replaceAll("\n",""));
            report.put("reasoncount",ef.value);


            String content = mapper.writer().writeValueAsString(report);
            sb.append(content);
            sb.append("\n");
        }

    }

    public long getSumBids() {
        return bid.sum();
    }

    public long getSumTotal() {
        return total.sum();
    }

    public List getMap() {
        Map x = new HashMap();
        List list = new ArrayList();

        /**
         * Sort the list first
         */
        List<EntryField> values = new ArrayList();
        for (Map.Entry<String, LongAdder> entry : probes.entrySet()) {
            EntryField ef = new EntryField(entry.getKey(), entry.getValue().sum());
            values.add(ef);
        }
        Collections.sort(values, Collections.reverseOrder());

        // Now create the map
        for (EntryField e : values) {
            x = new HashMap();
            x.put("name", e.key);
            x.put("count", e.value);
            list.add(x);
        }

        return list;
    }

    public String getTable() {
        double nobids = total.sum() - bid.sum();
        StringBuilder table = new StringBuilder("<table border='1'>");
        table.append("<tr><td>total</td><td>");
        table.append(total.sum());
        table.append("</td></tr>");
        table.append("<tr><td>bids</td><td>");
        table.append(bid.sum());
        table.append("</td></tr>");
        table.append("<tr><td>no bids:</td><td>");
        table.append((total.sum() - bid.sum()));
        table.append("</td></tr>");
        if (probes.entrySet().size() > 0) {
            table.append("<table>");
            table.append("<tr><td>Reasons</td><td><table border='1'><th>Reason</th><th>Count</th><th>Percent</th>");

            List<EntryField> values = new ArrayList();
            for (Map.Entry<String, LongAdder> entry : probes.entrySet()) {
                EntryField ef = new EntryField(entry.getKey(), entry.getValue().sum());
                values.add(ef);
            }

            Collections.sort(values, Collections.reverseOrder());

            for (EntryField e : values) {
                table.append("<tr><td>");
                table.append(e.key);
                table.append("</td><td>");
                table.append(e.value);
                table.append("</td><td>");
                table.append((e.value / nobids * 100));
                table.append("</td></tr>");
            }

            table.append("</table></td></tr></td></tr>");
        } else {
            table.append("</td></tr></td></tr>");
        }
        table.append("</table>");
        return table.toString();
    }
}
