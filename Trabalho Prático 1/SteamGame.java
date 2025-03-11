//campos do csv:
//appid,name,release_date,english,developer,publisher,platforms,required_age,categories,genres,steamspy_tags,achievements,positive_ratings,negative_ratings,average_playtime,median_playtime,owners,price

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SteamGame {
    private static final int PLATFORMS_LENGHT = 7; //tamanho da string de tamanho fixo em plataformas
    private int id; //id usado no arquivo de registros
    private int appid;
    private String name; //tamanho variável
    private Long release_date; //data, no formato ano-mes-dia
    private Boolean english; //boolean (0 ou 1)
    private String developer; //tamanho variável
    private String publisher; //tamanho variável
    private String platforms; //string de tamanho fixo
    private int required_age;
    private List<String> categories;  //lista de tamanho variável
    private List<String> genres;  //lista de tamanho variável
    private List<String> steamspy_tags;   //lista de tamanho variável
    private int achievements;
    private int positive_ratings;
    private int negative_ratings;
    private int average_playtime;
    private int median_playtime;
    private String owners; //tamanho variável (intervalo de números inteiros)
    private float price;

    //construtor (vazio)
    public SteamGame(){
        this.id = -1;
        this.appid = -1;
        this.name = "Space War";
        this.release_date = (long)1000000001;
        this.english = false;
        this.developer = "Valve";
        this.publisher = "Valve";
        this.platforms = "linux";
        this.required_age = 0;
        this.categories = new ArrayList<>();
        this.categories.add("Single-player");
        this.genres = new ArrayList<>();
        this.genres.add("Action");
        this.steamspy_tags = new ArrayList<>();
        this.steamspy_tags.add("FPS");
        this.achievements = -1;
        this.positive_ratings = 0;
        this.negative_ratings = 0;
        this.average_playtime = 0;
        this.median_playtime = 0;
        this.owners = "1 (Gabe)";
        this.price = 0;  
    }

    //construtor (recebendo List<String> como parâmetro)
    public SteamGame(int id, int appid, String name, Long release_date, Boolean english, String developer, String publisher, String platforms, int required_age, List<String> categories, List<String> genres, List<String> steamspy_tags, int achievements, int positive_ratings, int negative_ratings, int average_playtime, int median_playtime, String owners, float price) {
        this.id = id;
        this.appid = appid;
        this.name = name;
        this.release_date = release_date;
        this.english = english;
        this.developer = developer;
        this.publisher = publisher;
        this.platforms = platforms;
        this.required_age = required_age;
        this.categories = categories;
        this.genres = genres;
        this.steamspy_tags = steamspy_tags;
        this.achievements = achievements;
        this.positive_ratings = positive_ratings;
        this.negative_ratings = negative_ratings;
        this.average_playtime = average_playtime;
        this.median_playtime = median_playtime;
        this.owners = owners;
        this.price = price;
    }

    public int getId(){
        return id;
    }

    public boolean setId(int id){
        boolean response = false;
        //verificação de id diferente do id atual
        if (id != this.id && id > 0){
            this.id = id;
            response = true;
        }
        return response;
    }
    
    public int getAppid() {
        return appid;
    }

    public void setAppid(int appid) {
        this.appid = appid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getReleaseDateUnix() {
        return release_date;
    }

    public String getReleaseDateString() {
        //converte o timestamp para LocalDateTime
        LocalDateTime data = LocalDateTime.ofInstant(Instant.ofEpochSecond(release_date), ZoneOffset.UTC);
        
        //formata a data no formato AAAA-MM-DD
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return data.format(formatter);
    }

    public void setReleaseDate(Long release_date) {
        this.release_date = release_date;
    }

    public Boolean getEnglish() {
        return english;
    }

    public void setEnglish(Boolean english) {
        this.english = english;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPlatforms() {
        return platforms;
    }

    public boolean setPlatforms(String platforms) {
        boolean resp = false;
        if (platforms.length() == PLATFORMS_LENGHT){
            this.platforms = platforms;
            resp = true;
        }

        return resp;
    }

    public int getPlatformsLenght(){
        return PLATFORMS_LENGHT;
    }

    public int getRequiredAge() {
        return required_age;
    }

    public void setRequiredAge(int required_age) {
        this.required_age = required_age;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getSteamspyTags() {
        return steamspy_tags;
    }

    public void setSteamspyTags(List<String> steamspy_tags) {
        this.steamspy_tags = steamspy_tags;
    }

    public int getAchievements() {
        return achievements;
    }

    public void setAchievements(int achievements) {
        this.achievements = achievements;
    }

    public int getPositiveRatings() {
        return positive_ratings;
    }

    public void setPositiveRatings(int positive_ratings) {
        this.positive_ratings = positive_ratings;
    }

    public int getNegativeRatings() {
        return negative_ratings;
    }

    public void setNegativeRatings(int negative_ratings) {
        this.negative_ratings = negative_ratings;
    }

    public int getAveragePlaytime() {
        return average_playtime;
    }

    public void setAveragePlaytime(int average_playtime) {
        this.average_playtime = average_playtime;
    }

    public int getMedianPlaytime() {
        return median_playtime;
    }

    public void setMedianPlaytime(int median_playtime) {
        this.median_playtime = median_playtime;
    }

    public String getOwners() {
        return owners;
    }

    public void setOwners(String owners) {
        this.owners = owners;
    }

    public float getPrice() {
        return this.price;
    }

    public void setPrice(float price){
        this.price = price;
    }

    //calcular o tamanho do registro em bytes
    public int measureSize(){
        int soma = 0;
        
        // 2 int (id, appid)
        soma += 2 * Integer.BYTES;
    
        // 1 String (Name) - Tamanho de bytes de acordo com o UTF-8 para cada caracter
        soma += 2 + name.getBytes(StandardCharsets.UTF_8).length;
    
        // 1 long (ReleaseDate em UNIX)
        soma += Long.BYTES;
    
        // 1 boolean (English)
        soma += 1;
    
        // 3 Strings (Developer, Publisher, Platforms) - Tamanho de bytes de acordo com o UTF-8 para cada caracter
        soma += 2 + developer.getBytes(StandardCharsets.UTF_8).length;
        soma += 2 + publisher.getBytes(StandardCharsets.UTF_8).length;
        soma += 2 + platforms.getBytes(StandardCharsets.UTF_8).length;
    
        // 1 int (RequiredAge)
        soma += Integer.BYTES;
    
        // 1 int sinalizando a quantidade de elementos na lista
        soma += Integer.BYTES;
    
        // 1 lista (Categories): para cada elemento da lista: 2 bytes sinalizando tamanho da string, + tamanho de bytes de acordo com o UTF-8 para cada caracter
        for (String conta : categories){
            soma += 2 + conta.getBytes(StandardCharsets.UTF_8).length;
        }
        
        // 1 int sinalizando a quantidade de elementos na lista
        soma += Integer.BYTES;
    
        // 1 lista (Genres): para cada elemento da lista: 2 bytes sinalizando tamanho da string, + tamanho de bytes de acordo com o UTF-8 para cada caracter
        for (String conta : genres){
            soma += 2 + conta.getBytes(StandardCharsets.UTF_8).length;
        }
    
        // 1 int sinalizando a quantidade de elementos na lista
        soma += Integer.BYTES;
    
        // 1 lista (SteamSpyTags): para cada elemento da lista: 2 bytes sinalizando tamanho da string, + tamanho de bytes de acordo com o UTF-8 para cada caracter
        for (String conta : steamspy_tags){
            soma += 2 + conta.getBytes(StandardCharsets.UTF_8).length;
        }
    
        // 5 int (Achievements, PositiveRatings, Negative Ratings, AvaragePlaytime, MedianPlaytime)
        soma += 5 * Integer.BYTES;
    
        // 1 String (Owners) - 2 bytes + tamanho de bytes de acordo com o UTF-8 para cada caracter
        soma += 2 + owners.getBytes(StandardCharsets.UTF_8).length;
    
        // 1 float (preço)
        soma += Float.BYTES;
        
        return soma;
    }
    
    public void printAll() {
        System.out.println("--------------- Valores do Registro ---------------");
        System.out.println("ID: " + id);
        System.out.println("AppId: " + appid);
        System.out.println("Nome: " + name);
        System.out.println("Data de Lançamento: " + getReleaseDateString() + " (" + release_date + ")");
        System.out.println("Em Inglês: " + english);
        System.out.println("Desenvolvedor: " + developer);
        System.out.println("Publicador: " + publisher);
        System.out.println("Plataformas (apenas uma): " + platforms);
        System.out.println("Idade Requerida: " + required_age);
        System.out.println("Categorias: " + categories);
        System.out.println("Gêneros: " + genres);
        System.out.println("Steam SpyTags: " + steamspy_tags);
        System.out.println("Conquistas: " + achievements);
        System.out.println("Avaliações Positivas: " + positive_ratings);
        System.out.println("Avaliações Negativas: " + negative_ratings);
        System.out.println("Tempo Médio de Jogo: " + average_playtime);
        System.out.println("Tempo Mediano de Jogo: " + median_playtime);
        System.out.println("Proprietários: " + owners);
        System.out.println("Preço: " + price);
        System.out.println("----------------------------------------------------");
    }
}

