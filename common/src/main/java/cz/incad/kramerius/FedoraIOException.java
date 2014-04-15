/*
 * Copyright (C) 2013 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius;

import java.io.IOException;

public class FedoraIOException extends IOException {

    
    private int contentResponseCode;
    private String contentResponseBody;
    
    public FedoraIOException(int respCode, String respBody) {
        super();
        this.contentResponseBody = respBody;
        this.contentResponseCode = respCode;
    }

    public FedoraIOException(int respCode, String respBody,String message, Throwable cause) {
        super(message, cause);
        this.contentResponseBody = respBody;
        this.contentResponseCode = respCode;
    }

    public FedoraIOException(int respCode, String respBody,String message) {
        super(message);
        this.contentResponseBody = respBody;
        this.contentResponseCode = respCode;
    }

    public FedoraIOException(int respCode, String respBody,Throwable cause) {
        super(cause);
        this.contentResponseBody = respBody;
        this.contentResponseCode = respCode;
    }
        
    public String getContentResponseBody() {
        return contentResponseBody;
    }
    
    public int getContentResponseCode() {
        return contentResponseCode;
    }
}
