import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.StringBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryBuilder {

    private static final char [] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    
    public static void main(String[] args) {

        if(args.length == 0){
            System.out.println("Enter file name");
            return;
        }

        List<Listing> listings = new ArrayList<>();
        for(String fileName: args){
            listings.addAll(loadListings(fileName));
        }

        if(System.getenv("deleteListings") != null){
            deleteListings(listings);
            System.exit(0);
        }

        Map<String, Listing> listingDir = new HashMap<>();
        listings.stream()
            .forEach(listing -> listingDir.put(listing.getPath(), listing));

        Collections.sort(listings);
        generateListingHtmlFiles(listings);
        generateSitemap(listings);
        generateDirIndex(listings, listingDir);
        generateCategoryIndex(listingDir);

        System.out.println("Root directory: " + getDirRoot());
        System.out.printf("generated %d listings... ", listings.size());
    }

    private static void deleteListings(List<Listing> listings){

        listings.stream().forEach(listing -> {
            try{
                Path dir = Paths.get(getDirRoot(), listing.getPath());
                Path indexFile = Paths.get(dir.toString() + "/index.html");
                Files.deleteIfExists(indexFile);
                Files.deleteIfExists(dir);
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    private static String getDirRoot(){
        String rootDir = System.getenv("rootDir");
        return rootDir;
    }

    private static void generateCategoryIndex(Map<String, Listing> listingDir){

        var listingsMap = new HashMap<String, List<Listing>>();
        var categoryMap = new HashMap<>();

        listingDir.values().stream()
            .forEach(listing -> {

                String category = listing.getCategory();
                String [] categories = category.split(",");

                for(String cat: categories){
                    String key = getCategoryPath(cat);
                    categoryMap.putIfAbsent(key, cat);
                    listingsMap.putIfAbsent(key, new ArrayList<Listing>());
                    var categoryList = listingsMap.get(key);
                    categoryList.add(listing);
                }
            });

            listingsMap.entrySet().stream().forEach(e -> {
                generateCategoryPage(e.getKey(), e.getValue(), listingDir);
            });

            try{

                Path dir = Paths.get(getDirRoot(), "category");
                Path indexFile = Paths.get(dir.toString() + "/index.html");
                Files.deleteIfExists(indexFile);

                StringBuilder builder = new StringBuilder();
                builder.append(getHtmlHead());
                builder.append("<div class='container'>");
                builder.append("<div class='d-grid gap-2 d-md-block'>");
                listingsMap.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).forEach(e -> {
                    builder.append("<a style='margin:10px' class='btn btn-primary role='button' href='" + e.getKey() + "/'/>" +  categoryMap.get(e.getKey())  + "</a>");
                });
                builder.append("</div></div>");
                builder.append(getFooterHtml());

                BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile.toString()));
                writer.write(builder.toString());
                writer.close();
            
            }catch(Exception e){
                e.printStackTrace();
            }
    }

    private static void generateCategoryPage(String categoryPath, List<Listing> listings, Map<String, Listing> listingDir) {

        try {
            
            Path dir = Paths.get(getDirRoot(), "category/" + categoryPath);
            Path indexFile = Paths.get(dir.toString() + "/index.html");
            Files.deleteIfExists(indexFile);
            Files.deleteIfExists(dir);
            Files.createDirectories(dir);
            createCategoryFile(indexFile, listings, listingDir);
                
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void createCategoryFile(Path indexFile, List<Listing> listings, Map<String, Listing> listingDir) throws IOException{

        Collections.sort(listings);

        Files.createFile(indexFile);
        
        try{         
            
            StringBuilder builder = new StringBuilder();
            builder.append(getHtmlHead());
            builder.append("<ul class='list-group'>");

            for (Listing listing : listings) {
                builder.append("<li class='list-group-item'>");
                builder.append("<div class='card'/>");
                builder.append("<div class='card-header'><a href='/" + listing.getPath() + "'/>" + listingDir.get(listing.getPath()) + "</a></div>");
                builder.append("<div class='card-body'>" + listing.getAddress() + "</div");
                builder.append("</div>");
                builder.append("</li>");
                builder.append("\n");
            }

            builder.append("</ul>");
            builder.append(getFooterHtml());
            BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile.toString()));
            writer.write(builder.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static String getCategoryPath(String category){
        return category.replaceAll(",", "-")
        .replaceAll("/", "-")
        .replaceAll(" &", "")
        .replaceAll(" ", "-")
        .toLowerCase();

    }

    private static String getFooterHtml(){
        return """      
                    </div>
                    </main>
    
                        <footer>
                            <div class="text-center p-4" style="background-color: rgba(0, 0, 0, 0.01);">
                                    <small> Copyright © 2023 <a class="text-reset fw-bold" href="https://sylhetdirectory.com/">sylhetdirectory.com</a></small>
                                </div>
                        </footer>
    
                    </body>

                </html>""";

    }

    private static String getHtmlHead(){

        return """
                <!DOCTYPE html>
                   <html lang="en">
                   
                   <head>
                       <title>Sylhet Directory</title>
                       <meta charset="UTF-8" />
                       <meta name="viewport" content="width=device-width, initial-scale=1">
                       <meta http-equiv="X-UA-Compatible" content="ie=edge" />
                       <meta name="description" content="Sylhet Directory is Sylhet's leading internet business index. Find the service you require by searching or by browsing.">
                       <link rel="stylesheet" href="/css/main.css">
                   
                       <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
                       <script src="https://cdn.jsdelivr.net/npm/popper.js@1.14.6/dist/umd/popper.min.js" integrity="sha384-wHAiFfRlMFy6i5SRaxvfOCifBUQy1xHdJ/yoi7FRNXMRBu5WHdZYu1hA6ZOblgut" crossorigin="anonymous"></script>
                       <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.2.1/dist/css/bootstrap.min.css" integrity="sha384-GJzZqFGwb1QTTN6wy59ffF1BuGJpLSa9DkKMp0DgiMDm4iYMj70gZWKYbI706tWS" crossorigin="anonymous">
                       <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.2.1/dist/js/bootstrap.min.js" integrity="sha384-B0UglyR+jN6CkvvICOB2joaf5I4l3gm9GU6Hc1og6Ls7i6U/mkkaduKaBhlAXv9k" crossorigin="anonymous"></script>
                   
                       <!-- Global site tag (gtag.js) - Google Analytics -->
                       <script async src="https://www.googletagmanager.com/gtag/js?id=UA-27081730-1"></script>
                       <script>
                           window.dataLayer = window.dataLayer || [];
                           function gtag() { dataLayer.push(arguments); }
                           gtag('js', new Date());
                           gtag('config', 'UA-27081730-1');
                       </script>
                   </head>
                   
                   <body>
                       <header>
                           <nav class="navbar navbar-expand-lg navbar-light bg-light">
                               <a class="navbar-brand" href="/">Sylhet Directory</a>
                               <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                                   <span class="navbar-toggler-icon"></span>
                               </button>
                               <div class="collapse navbar-collapse" id="navbarNav">
                                   <ul class="navbar-nav active">
                                       <li class="nav-item">
                                           <a class="nav-link" href="/about/">About<span class="sr-only">(current)</span></a>
                                       </li>
                                       <li class="nav-item">
                                           <a class="nav-link" href="https://docs.google.com/forms/d/e/1FAIpQLScYHs_JktxOMwxYG00Sg3j_s6uFMOOiqTdLBednautxgIEu7g/viewform">Register business</a>
                                       </li>
                                       <li class="nav-item">
                                           <a class="nav-link" href="/contact/">Contact us</a>
                                       </li>
                                   </ul>
                               </div>
                           </nav>
                       </header>
                       <main>
                           <div class="content">
                           
                           """;
    }
    

private static void generateDirIndex(List<Listing> listings, Map<String, Listing> listingDir) {

        try {

            StringBuilder builder = new StringBuilder();
            builder.append(getHtmlHead());
            builder.append("<div class='container'>");
            builder.append("<div class='d-grid gap-2 d-md-block'>");

            for (char letter : alphabet) {

                Path dir = Paths.get(getDirRoot(), "a-z/" + letter);
                Path indexFile = Paths.get(dir.toString() + "/index.html");
                Files.deleteIfExists(indexFile);
                Files.deleteIfExists(dir);
                Files.createDirectories(dir);

                builder.append(String.format("<a style='margin:10px' href='/a-z/%s' class='btn btn-primary' role='button'> %s </a>", letter, letter));

                createFile(
                        indexFile, letter,
                        listings.stream()
                                .filter(listing -> letter == listing.getTitle().charAt(0)).collect(Collectors.toList()),
                        listingDir);
            }

            Path dirIndex = Paths.get(getDirRoot(), "a-z/");
            Path indexFile = Paths.get(dirIndex.toString() + "/index.html");
            Files.deleteIfExists(indexFile);

            builder.append("</div></div></div>");
            builder.append(getFooterHtml());

            BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile.toString()));
            writer.write(builder.toString());
            writer.close();

                
        } catch (Exception e){
            e.printStackTrace();
        }
    }

private static void createFile(Path indexFile, char letter, List<Listing> listings, Map<String, Listing> listingDir) throws IOException{
   

        Files.createFile(indexFile);
        
        try{ 
            Comparator<Listing> pathComparator = (h1, h2) -> h1.getPath().compareTo(h2.getPath());
            listings = listings.stream().sorted(pathComparator).collect(Collectors.toList());
            
            StringBuilder builder = new StringBuilder();

            builder.append(getHtmlHead());
            builder.append("<ul class='list-group'>");

            for (Listing listing : listings) {
                builder.append("<li class='list-group-item'>");
                builder.append("<div class='card'/>");
                builder.append("<div class='card-header'><a href='/" + listing.getPath() + "'/>" + listingDir.get(listing.getPath()) + "</a></div>");
                builder.append("<div class='card-body'>" + listing.getAddress() + "</div>");
                builder.append("<div class='card-footer'><small>Category: <a class='badge badge-primary' href='/category/" + listing.getCategory()
                        .replace(" &", "")
                        .replace(" ", "-")
                        .toLowerCase()
                        + "'>" + listing.getCategory() + "</a></small></div>");
                builder.append("</div>");
                builder.append("</li>");
                builder.append("\n");
            }

            builder.append("</ul>");
            builder.append("<div style='margin:20px'>");
            builder.append("</div>");
            builder.append(getFooterHtml());
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile.toString()));
            writer.write(builder.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static String capitalise(String text){

        StringBuilder builder = new StringBuilder();
        String [] tokens = text.split(" ");

        for(String token: tokens){

            if(token.length() < 2){
                builder.append(token);
                builder.append("\n");
            } else {
                builder.append(token.substring(0,1).toUpperCase() + token.substring(1));
                builder.append("\n");
            }           
        }

        return builder.toString().trim();
    }

    public static void generateListingHtmlFiles(List<Listing> listings) {

        listings.stream().forEach(listing -> {

            try {

                Path dir = Paths.get(getDirRoot(), listing.getPath());
                Path indexFile = Paths.get(dir.toString() + "/index.html");
                Files.deleteIfExists(indexFile);
                Files.deleteIfExists(dir);
                Files.createDirectories(dir);
                
                Path source = Paths.get("listingTemplate.html");
                Path target = indexFile;
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

                    Stream<String> lines = Files.lines(target);
                    List<String> replaced = lines
                            .map(line -> line.replaceAll("listing-meta-description", getDescriptionMeta(listing.getAddress())))
                            .map(line -> line.replaceAll("listing-title", listing.getTitle()))
                            .map(line -> line.replaceAll("listing-address", listing.getAddress()))
                            .map(line -> line.replaceAll("listing-contact", getContactsWithTracking(listing.getContact())))
                            .map(line -> line.replaceAll("listing-facebook", getFacebookHtml(listing.getFacebook())))
                            .map(line -> line.replaceAll("listing-category", listing.getCategory()))
                            .map(line -> line.replaceAll("category-slug", listing.getCategory()
                                    .replace(" &", "")
                                    .replace(" ", "-")
                                    .toLowerCase()))
                            .map(line -> line.replace("listing-meta-category", getCategoryMeta(listing.getCategory())))
                            .collect(Collectors.toList());

                    Files.write(target, replaced);
                    lines.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private static String getCategoryMeta(String category){
        String html = "";
        if(!category.isEmpty()){
            html = "<meta name='category' content='" + category + "'>";
        }
        return html;
    }

    private static String getDescriptionMeta(String description){
        String html = "";
        if(!description.isEmpty()){
            html = "<meta name='description' content='" + description +"'>";
        }
        return html;
    }

    private static String getContactsWithTracking(String contacts) {

        StringBuilder builder = new StringBuilder();
        String[] numbers = contacts.split(",");
        for (String number : numbers) {
            if (!number.isEmpty()) {
                builder.append("<p><a role='button' class='btn btn-primary' href='tel:" + number + "' onclick='gtag_report_conversion('" + number + "')'> " + number.replaceFirst("\\+88", "") + "</a><p>");
            }
        }
        return builder.toString();
    }

    private static String getFacebookHtml(String facebook){
        if(!facebook.isEmpty()) return "<p><a role='button' class='btn btn-primary' href='"+ facebook +"' class='badge badge-primary'>Facebook</a><p>";
        return "";
    }

    public static List<Listing> loadListings(final String listingsCsv) {

        List<Listing> listings = new ArrayList<>();

        try (BufferedReader csvReader = new BufferedReader(new FileReader(listingsCsv))) {

            String row = "";
            while ((row = csvReader.readLine()) != null) {
//                System.out.println(row);
                if(row.isBlank()){
                    continue;
                }

                row = row.replaceAll("&amp;", "&");
                row = row.replaceAll("\\\"", "");
                row = row.replaceAll("NULL", "");
                row = row.replaceAll("\n", "");
                String[] cols = row.split("\t");

                Listing listing = new Listing(
                        cols[1].replaceAll("\\\"", ""),
                        cols[2],
                        cols[3].replaceAll("\\\"", ""),
                        cols[4].replaceAll("\\\"", ""),
                        cols[5],
                        cols[6].contains("facebook.com")? cols[6]: "",
                        cols[7],
                        cols[8],
                        cols[9],
                        cols[10]);

               // System.out.println("facebook " + listing.getFacebook());

                listings.add(listing);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Listing> filtered = listings.stream()
            .filter(listing -> !listing.getPath().equals("Path"))
            .collect(Collectors.toList());

        return filtered;
    }

    public static void generateSitemap(List<Listing> listings) {

        try {

            Path listingsXml = Paths.get(getDirRoot(), "listings.xml");
            Files.deleteIfExists(listingsXml);

            StringBuilder builder = new StringBuilder();
            builder.append("<?xml version='1.0' encoding='UTF-8'?>");
            builder.append("\n");
            builder.append("<urlset xmlns='http://www.sitemaps.org/schemas/sitemap/0.9'>");
            builder.append("\n");

            for (Listing listing : listings) {

                String path = listing.getPath();
                builder.append("<url>");
                builder.append("<loc>https://sylhetdirectory.com/"
                        + path.toString().replace("./", "").replaceAll("&", "&amp;") + "</loc>");
                builder.append("<lastmod>" + LocalDate.now() + "</lastmod>");
                builder.append("</url>");
                builder.append("\n");
            }
            builder.append("</urlset>");

            BufferedWriter writer = new BufferedWriter(new FileWriter(listingsXml.toAbsolutePath().toString()));
            writer.write(builder.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

class Listing implements Comparable<Listing> {
    private final String title;
    private final String contact;
    private final String address;
    private final String email;
    private final String web;
    private final String facebook;
    private final String service;
    private final String openingHrs;
    private final String category;
    private final String path;



    public Listing(final String title,
                   final String contact,
                   final String address,
                   final String email,
                   final String web,
                   final String facebook,
                   final String service,
                   final String openingHrs,
                   final String category,
                   final String path ) {
        this.title = title;
        this.contact = contact;
        this.address = address;
        this.email = email;
        this.web = web;
        this.facebook = facebook;
        this.service = service;
        this.openingHrs = openingHrs;
        this.category = category;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getContact() {
        List<String> contacts = Arrays.asList(contact.split(","));
        return contacts.stream()
        .map(contact -> contact.replace(" ", ""))
        .filter(contact -> contact.length() > 9)
        .map(contact -> contact.replace("+88", ""))
        .map(contact -> contact.replace("0088", ""))
        .map(contact -> contact.replaceFirst("0", ""))
        .map(contact -> "+880" + contact)
        .collect(Collectors.joining(","));
        
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getWeb() {
        return web;
    }

    public String getFacebook() {
        return facebook;
    }

    public String getService() {
        return service;
    }

    public String getOpeningHrs() {
        return openingHrs;
    }

public String getCategory() {
        return category;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int compareTo(Listing listing) {
        return this.path.split("-")[0]
                .compareTo(listing.path.split("-")[0]);
    }

    @Override
    public String toString(){
        return this.title;
    }
}