package haipeng.myalbum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import haipeng.myalbum.entity.Album;
import haipeng.myalbum.imageload.ImageFetcher;

/**
 * Created by Administrator on 2015/8/14.
 */
public class AlbumAdapter extends BaseAdapter{

    Context mContext;
    List<Album> mList;
    ImageFetcher mImageFetcher;
    public AlbumAdapter(Context context, List<Album> list,ImageFetcher imageFetcher){
        mList = list;
        mList.add(getButtonAlbum());
        mContext = context;
        mImageFetcher = imageFetcher;
    }
    public Album getButtonAlbum(){
        String title = "";
        String path = "";
        Album album = new Album();
        album.setPath(path);
        album.setName(title);
        return album;
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_album,null);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.item_album_iv);
        TextView  textView  = (TextView) convertView.findViewById(R.id.item_album_tv);
        Button    btn       = (Button) convertView.findViewById(R.id.item_album_btn);
        String image_path = mList.get(position).getPath();
        String tv_path    = mList.get(position).getName();
        if(!"".equals(image_path)&&!"".equals(tv_path)) {
            mImageFetcher.loadImage(image_path, imageView, true);
            textView.setText(tv_path);
        }
        if(position == mList.size()-1)
        {
            btn.setVisibility(View.VISIBLE);

            btn.setOnClickListener((EntranceActivity)mContext);
        }
        return convertView;
    }

}
