package practiceschool.music;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

public class MusicLoader {
	
	private static final String TAG = "zhihu.music.MusicLoader";
	
	private static List<MusicInfo> musicList = new ArrayList<MusicInfo>();
	
	private static MusicLoader musicLoader;
	
	private static ContentResolver contentResolver;
	
	private Uri contentUri = Media.EXTERNAL_CONTENT_URI;	
	
	private String[] projection = {
			Media._ID,
			Media.TITLE,
			Media.DATA,
			Media.ALBUM,
			Media.ARTIST,
			Media.DURATION,			
			Media.SIZE
	};
	private String where =  "mime_type in ('audio/mpeg','audio/x-ms-wma') and bucket_display_name <> 'audio' and is_music > 0 " ;
	private String sortOrder = Media.DATA;
	
	
	//���ǵ���ģʽ��һ�����ڱȽϴ󣬸��ӵĶ���ֻ��ʼ��һ�Σ�Ӧ�û���һ��private�Ĺ��캯��;
	//ʹ�ò�����new��ʵ��������ֻ�ܵ���getInstance�������õ����󣬶�getInstance��֤��ÿ�ε��ö�������ͬ�Ķ���;
	public static MusicLoader instance(ContentResolver pContentResolver){
		if(musicLoader == null){
			contentResolver = pContentResolver;
			musicLoader = new MusicLoader();			
		}
		return musicLoader;
	}
	
	//����ģʽ��MusicLoader��Ĺ��캯��
	private MusicLoader(){		
		Cursor cursor = contentResolver.query(contentUri, null, null, null, null);   //��ѯ����������Դ
		if(cursor == null){
			Log.v(TAG,"Line(37	)	Music Loader cursor == null.");
		}else if(!cursor.moveToFirst()){
			Log.v(TAG,"Line(39	)	Music Loader cursor.moveToFirst() returns false.");
		}else{			 
			int displayNameCol = cursor.getColumnIndex(Media.TITLE);
			int albumCol = cursor.getColumnIndex(Media.ALBUM);
			int idCol = cursor.getColumnIndex(Media._ID);
			int durationCol = cursor.getColumnIndex(Media.DURATION);
			int sizeCol = cursor.getColumnIndex(Media.SIZE);
			int artistCol = cursor.getColumnIndex(Media.ARTIST);
			int urlCol = cursor.getColumnIndex(Media.DATA);		
			do{
				String title = cursor.getString(displayNameCol);
				if(title.equals("")){continue;};    //���titleΪ�գ����׸費���뵽�����б���
				String album = cursor.getString(albumCol);
				long id = cursor.getLong(idCol);				
				int duration = cursor.getInt(durationCol);
				if(duration == 0){continue;};
				long size = cursor.getLong(sizeCol);
				if(size == 0){continue;};
				String artist = cursor.getString(artistCol);
				String url = cursor.getString(urlCol);	
				MusicInfo musicInfo = new MusicInfo(id, title);
				musicInfo.setAlbum(album); 
				musicInfo.setDuration(duration);
				musicInfo.setSize(size);
				musicInfo.setUrl(url);
				if(title.equals("yueshu")){
					musicInfo.setTitle("���տ���Լ��");
					musicInfo.setArtist("�ػ���");
					musicList.add(0,musicInfo);
				}else if(title.equals("anheqiao")){
					musicInfo.setTitle("������");
					musicInfo.setArtist("�ζ�Ұ");
					musicList.add(1,musicInfo);
				}else if(title.equals("guxiang")){
					musicInfo.setTitle("����");
					musicInfo.setArtist("��������&��׿");
					musicList.add(2,musicInfo);
				}else if(title.equals("lilian")){
					musicInfo.setTitle("����");
					musicInfo.setArtist("�ζ�Ұ");
					musicList.add(3,musicInfo);
				}else if(title.equals("banma")){
					musicInfo.setTitle("�������");
					musicInfo.setArtist("�ζ�Ұ");
					musicList.add(4,musicInfo);
				}else{
					musicInfo.setArtist(artist);
					musicList.add(musicInfo);
				} 
				
			}while(cursor.moveToNext());
		}
	}
	 
	public List<MusicInfo> getMusicList(){
		return musicList;
	}
	
	public Uri getMusicUriById(long id){
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		return uri;
	}	
	
	public static String formatDuration(int milliseconds){
		int seconds = milliseconds / 1000;
		int secondPart = seconds % 60;
		int minutePart = seconds / 60;
		return (minutePart >= 10 ? minutePart : "0" + minutePart) + ":" + (secondPart >= 10 ? secondPart : "0" + secondPart);
	}
	
	public static String formatTitle(String title, int length){
		int len = title.length() < length ? title.length():length;		
		String subString = title.substring(0, len);
		if(len < title.length()){
			subString += "...";
		}
		return subString;
	}

	//ʵ��Parcelable����Ϊ�˽������л����������ݴ���
	public static class MusicInfo implements Parcelable{										
		private long id;
		private String title;
		private String album;
		private int duration;
		private long size;
		private String artist;		
		private String url;		
		
		public MusicInfo(){
			
		}
		
		public MusicInfo(long pId, String pTitle){
			id = pId;
			title = pTitle;
		}
		
		public String getArtist() {
			return artist;
		}

		public void setArtist(String artist) {
			this.artist = artist;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}		

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getAlbum() {
			return album;
		}

		public void setAlbum(String album) {
			this.album = album;
		}

		public int getDuration() {
			return duration;
		}

		public void setDuration(int duration) {
			this.duration = duration;
		}	

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		//���������ӿڣ��������ù�
		public int describeContents() {
			return 0;
		}

		//д��ӿں��������
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(id);
			dest.writeString(title);
			dest.writeString(album);
			dest.writeString(artist);
			dest.writeString(url);
			dest.writeInt(duration);
			dest.writeLong(size);
		}
		
		//��ȡ�ӿڣ�Ŀ����Ҫ��Parcel�й���һ��ʵ����Parcelable�����ʵ��������Ϊʵ���������ﻹ�ǲ���֪�ģ�������Ҫ�õ�ģ��ķ�ʽ���̳�����ͨ��ģ���������
	    //Ϊ���ܹ�ʵ��ģ������Ĵ��룬���ﶨ��CreatorǶ��ӿ�,�ں������ӿں����ֱ𷵻ص����Ͷ���̳���ʵ��
		public static final Parcelable.Creator<MusicInfo> 
			CREATOR = new Creator<MusicLoader.MusicInfo>() {
			
			@Override
			public MusicInfo[] newArray(int size) {
				return new MusicInfo[size];
			}
			
			@Override
			public MusicInfo createFromParcel(Parcel source) {
				MusicInfo musicInfo = new MusicInfo();
				musicInfo.setId(source.readLong());
				musicInfo.setTitle(source.readString());
				musicInfo.setAlbum(source.readString());
				musicInfo.setArtist(source.readString());
				musicInfo.setUrl(source.readString());
				musicInfo.setDuration(source.readInt());
				musicInfo.setSize(source.readLong());
				return musicInfo;
			}
		};
	}
}
