package haipeng.myalbum.entity;

/**
 * Created by Administrator on 2015/8/14.
 */
public class Album {
    String image_name ;
    String isParent ;
    String isChild ;
    String folderPath;

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public void setIsParent(String isParent) {
        this.isParent = isParent;
    }

    public void setIsChild(String isChild) {
        this.isChild = isChild;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getImage_name() {
        return image_name;
    }

    public String getIsParent() {
        return isParent;
    }

    public String getIsChild() {
        return isChild;
    }
}
