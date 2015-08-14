package haipeng.myalbum.entity;

/**
 * Created by Administrator on 2015/8/14.
 */
public class Album {
    private String name;
    private String path;

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
