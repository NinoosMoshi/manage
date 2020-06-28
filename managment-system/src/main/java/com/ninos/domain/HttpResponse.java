package com.ninos.domain;

import java.util.Date;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class HttpResponse {
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyy hh:mm:ss", timezone = "America/Chicago")   // "timeStamp": "06-20-2020 03:34:22"
	private Date timeStamp;
	
	private int httpStatusCode;   // ex: 200, 201, 400, 500
	private HttpStatus httpStatis;  // ex: OK
	private String reason;      //  ex: inside HttpStatus there is INTERNAL_SERVER_ERROR(500, "Internal Server Error") , so reason is "Internal Server Error"
	private String message;    // ex: this it will developer message (your message) like : "your request is created"
	
	public HttpResponse(int httpStatusCode, HttpStatus httpStatis, String reason, String message) {
		this.timeStamp = new Date();
		this.httpStatusCode = httpStatusCode;
		this.httpStatis = httpStatis;
		this.reason = reason;
		this.message = message;
	}
	
		

}


