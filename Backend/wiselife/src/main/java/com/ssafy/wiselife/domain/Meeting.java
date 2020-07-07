package com.ssafy.wiselife.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "meeting")
public class Meeting {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int meetingId;
	
	@Column(columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
	private String writer;
	
	@Column(columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
	private String title;
	
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date createdAt;
	
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date updatedAt;
	
	private int isPeriod;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date meetingDate;
	
	@Column(columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
	private String periodDate;
	
	private int isClass;
	private int maxPerson;
	private int nowPerson;
	
	@Column(columnDefinition = "LONGTEXT CHARACTER SET utf8mb4")
	private String content;
	
	@Column(columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
	private String refUrl;
	
	@Column(columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
	private String address;
	
	private int fee;
	
	@Column(columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
	private String unit;
	
	private int isActive;
	private int likeCnt;
	private int viewCnt;
	private double score;
	
	@Column(columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
	private String tags;
	@Column(columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
	private String area1;
	@Column(columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
	private String area2;
	@Column(columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
	private String phone;
	
	@ManyToOne
	@JoinColumn(name = "uid")
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "mainCategory", referencedColumnName = "categoryId")
	private Category category;
	
	@OneToMany(mappedBy = "meeting")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private List<Review> reviewList = new ArrayList<>();
	
	@OneToMany(mappedBy = "meeting")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private List<UserMeeting> userMeetingList = new ArrayList<>();
	
	@OneToMany(mappedBy = "meeting")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private List<LikeMeeting> likeMeetingList = new ArrayList<>();
	
	@OneToMany(mappedBy = "meeting")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private List<MeetingImages> meetingImagesList;
	
	@Override //JPA 연관관계 설정시 toString의 무한반복 호출을 막기위해 사용
	 public String toString() {
	     return ToStringBuilder
	     .reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
