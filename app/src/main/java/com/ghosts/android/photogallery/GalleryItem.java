package com.ghosts.android.photogallery;

import java.util.List;

/**
 * Created by Allam on 17/03/2016.
 */
public class GalleryItem {


    private PhotosBean photos;

    public PhotosBean getPhotos() {
        return photos;
    }

    public void setPhotos(PhotosBean photos) {
        this.photos = photos;
    }

    public static class PhotosBean {
        private int page;
        private int pages;

        private List<PhotoBean> photo;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public List<PhotoBean> getPhoto() {
            return photo;
        }

        public void setPhoto(List<PhotoBean> photo) {
            this.photo = photo;
        }

        public static class PhotoBean {
            private String id;
            private String title;
            private String url_s;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getUrl_s() {
                return url_s;
            }

            public void setUrl_s(String url_s) {
                this.url_s = url_s;
            }
        }
    }
}



