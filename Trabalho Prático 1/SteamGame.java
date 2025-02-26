//campos do csv:
//appid,name,release_date,english,developer,publisher,platforms,required_age,categories,genres,steamspy_tags,achievements,positive_ratings,negative_ratings,average_playtime,median_playtime,owners,price

import java.util.List;

public class SteamGame {
    protected int appid;
    protected String name; //tamanho variável
    protected String release_date; //data, no formato ano-mes-dia
    protected Boolean english; //boolean (0 ou 1)
    protected String developer; //tamanho variável
    protected String publisher; //tamanho variável
    protected String platforms; //string de tamanho fixo ("windows")
    protected int required_age;
    protected List<String> categories;  //lista de tamanho variável
    protected List<String> genres;  //lista de tamanho variável
    protected List<String> steamspy_tags;   //lista de tamanho variável
    protected int achievements;
    protected int positive_ratings;
    protected int negative_ratings;
    protected int average_playtime;
    protected int median_playtime;
    protected String owners; //tamanho variável (intervalo de números inteiros)
    protected float price;

    //construtor (vazio)
    public SteamGame(){
        this.appid = -1;
        this.name = "Space War";
        this.release_date = "0000-01-01";
        this.english = false;
        this.developer = "Valve";
        this.publisher = "Valve";
        this.platforms = "linux";
        this.required_age = 0;
        this.categories.add("Single-player");
        this.genres.add("Action");
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
    public SteamGame(int appid, String name, String release_date, Boolean english, String developer, String publisher, String platforms, int required_age, List<String> categories, List<String> genres, List<String> steamspy_tags, int achievements, int positive_ratings, int negative_ratings, int average_playtime, int median_playtime, String owners, float price) {
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
}

