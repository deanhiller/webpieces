package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import java.util.LinkedList;
import java.util.List;

public interface HasHeaderFragment {
    class Header {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Header header1 = (Header) o;

            if (header != null ? !header.equals(header1.header) : header1.header != null) return false;
            return value != null ? value.equals(header1.value) : header1.value == null;

        }

        @Override
        public int hashCode() {
            int result = header != null ? header.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        public Header(String header, String value) {
            this.header = header;
            this.value = value;
        }

        public String header;
        public String value;
    }
    boolean isEndHeaders();

    void setEndHeaders(boolean endHeaders);

    DataWrapper getHeaderFragment();
    void setHeaderFragment(DataWrapper fragment);
}
