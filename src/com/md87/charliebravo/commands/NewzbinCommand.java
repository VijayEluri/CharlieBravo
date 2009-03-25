/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo.commands;

import com.dmdirc.util.Downloader;
import com.dmdirc.util.MapList;
import com.md87.charliebravo.Command;
import com.md87.charliebravo.Followup;
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author chris
 */
public class NewzbinCommand implements Command {

    protected static final String URL = "/search/query/?q=%s" +
            "&area=-1&fpn=p&searchaction=Go&areadone=-1&sort=date&order=desc" +
            "&feed=rss";

    protected static final String LOGIN_URL = "https://www.newzbin.com/account/login";

    @SuppressWarnings("unchecked")
    public void execute(InputHandler handler, Response response, String line) throws Exception {
        if (line.isEmpty()) {
            response.sendMessage("You need to specify a search query", true);
        } else {
            final Map<String, String> args = new HashMap<String, String>();
            args.put("ret_url", String.format(URL,
                    URLEncoder.encode(line, Charset.defaultCharset().name())));
            args.put("username", "dataforce");
            args.put("password", "dfrox");
            final List<String> result = Downloader.getPage(LOGIN_URL, args);
            final StringBuilder data = new StringBuilder();

            for (String resultline : result) {
                data.append(resultline);
                data.append('\n');
            }

            System.out.println(data);

            final Document document = new SAXBuilder().build(new StringReader(data.toString()));
            final List<Result> results = new LinkedList<Result>();

            for (Element item : (List<Element>) document.getRootElement()
                    .getChild("channel").getChildren("item")) {
                results.add(new Result(item));
            }

            response.sendMessage("there " + (results.size() == 1 ? "was" : "were")
                    + " " + results.size() + " result" + (results.size() == 1 ? "s" : "")
                    + " returned." + (results.size() == 1 ? "It is: " : "The first one is: ")
                    + results.get(0).getSummary());
            response.addFollowup(new AttributesFollowup(results.get(0)));
            response.addFollowup(new NextFollowup(results, 1));
        }
            
    }

    protected static class NextFollowup implements Followup {

        private final List<Result> results;
        private final int next;

        public NextFollowup(List<Result> results, int next) {
            this.results = results;
            this.next = next;
        }

        public boolean matches(String line) {
            return next < results.size() && line.equalsIgnoreCase("next");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            response.sendMessage("result " + next + "/" + results.size() + "is: "
                    + results.get(next).getSummary());
            response.addFollowup(new AttributesFollowup(results.get(next)));
            response.addFollowup(new NextFollowup(results, next + 1));
            response.addFollowup(new PreviousFollowup(results, next - 1));
        }

    }

    protected static class PreviousFollowup implements Followup {

        private final List<Result> results;
        private final int prev;

        public PreviousFollowup(List<Result> results, int next) {
            this.results = results;
            this.prev = next;
        }

        public boolean matches(String line) {
            return prev > 0 && line.equalsIgnoreCase("previous");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            response.sendMessage("result " + prev + "/" + results.size() + "is: "
                    + results.get(prev).getSummary());
            response.addFollowup(new AttributesFollowup(results.get(prev)));
            response.addFollowup(new NextFollowup(results, prev + 1));
            response.addFollowup(new PreviousFollowup(results, prev - 1));
        }

    }

    protected static class AttributesFollowup implements Followup {

        private final Result result;

        public AttributesFollowup(Result result) {
            this.result = result;
        }

        public boolean matches(String line) {
            return line.equalsIgnoreCase("attributes");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            final StringBuilder builder = new StringBuilder();

            for (Map.Entry<String, List<String>> entry : result.getAttributes().entrySet()) {
                if (builder.length() > 0) {
                    builder.append("; ");
                }

                builder.append(entry.getKey() + ": ");

                boolean first = true;
                for (String value : entry.getValue()) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(", ");
                    }

                    builder.append(value);
                }
            }

            response.setInheritFollows(true);
            response.sendMessage("that result has the following attributes: " + builder.toString());
        }

    }

    protected static class Result {

        protected static final String REPORT_NS = "http://www.newzbin.com/DTD/2007/feeds/report/";
        protected static final String NZB_URL = "http://www.newzbin.com/browse/post/%s/nzb/";

        private final String title, category, moreinfo, nfolink, poster, date;
        private final MapList<String, String> attributes = new MapList<String, String>();
        private final List<String> groups = new ArrayList<String>();
        private final int id, nfoid, views, comments;
        private final long size;

        public Result(final Element element) {
            final Namespace namespace = Namespace.getNamespace(REPORT_NS);

            title = element.getChildTextTrim("title");
            id = Integer.parseInt(element.getChildTextTrim("id", namespace));
            category = element.getChildTextTrim("category", namespace);

            for (Object attribute : element.getChildren("attributes", namespace)) {
                final String type = ((Element) attribute).getAttributeValue("type");
                final String value = ((Element) attribute).getTextTrim();
                attributes.add(type, value);
            }

            for (Object group : element.getChildren("groups", namespace)) {
                groups.add(((Element) group).getTextTrim());
            }

            moreinfo = element.getChildTextTrim("moreinfo", namespace);

            nfoid = Integer.parseInt(element.getChild("nfo", namespace)
                    .getChildText("fileid", namespace));
            nfolink = element.getChild("nfo", namespace)
                    .getChildText("link", namespace);
            poster = element.getChildText("poster", namespace);
            size = Long.parseLong(element.getChildTextTrim("size", namespace));
            date = element.getChildText("postdate", namespace);
            views = Integer.parseInt(element.getChild("stats", namespace)
                    .getChildText("views", namespace));
            comments = Integer.parseInt(element.getChild("stats", namespace)
                    .getChildText("comments", namespace));
        }

        public String getSummary() {
            return "'" + title + "' (" + getSizeMB() + "MiB), nzb link: "
                    + String.format(NZB_URL, id);
        }

        public String getSizeMB() {
            return String.format("%,.2f", size / (1024 * 1024));
        }

        public MapList<String, String> getAttributes() {
            return attributes;
        }

        public String getCategory() {
            return category;
        }

        public int getComments() {
            return comments;
        }

        public String getDate() {
            return date;
        }

        public List<String> getGroups() {
            return groups;
        }

        public int getId() {
            return id;
        }

        public String getMoreinfo() {
            return moreinfo;
        }

        public int getNfoid() {
            return nfoid;
        }

        public String getNfolink() {
            return nfolink;
        }

        public String getPoster() {
            return poster;
        }

        public long getSize() {
            return size;
        }

        public String getTitle() {
            return title;
        }

        public int getViews() {
            return views;
        }

    }

}
