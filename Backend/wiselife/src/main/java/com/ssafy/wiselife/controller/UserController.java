package com.ssafy.wiselife.controller;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.wiselife.dto.SurveyDTO;
import com.ssafy.wiselife.dto.UserDTO;
import com.ssafy.wiselife.service.IKakaoService;
import com.ssafy.wiselife.service.IUserService;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = ("*"), maxAge = 6000)
@RequestMapping("/api")
@RestController
public class UserController {

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private IUserService userservice;

	@Autowired
	private IKakaoService kakaoservice;

	@GetMapping("/user/login")
	@ApiOperation(value = "로그인하기")
	public ResponseEntity<Map<String, Object>> login(HttpServletRequest req, HttpServletResponse res) {

		String access_token = req.getHeader("access_token");

		System.out.println("access_token : " + access_token);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HttpStatus status = null;

		HashMap<String, Object> userInfo = kakaoservice.getUserInfo(access_token);

		long uid = (long) userInfo.get("id");
		String nickname = userInfo.get("nickname").toString();
		String profile_image = userInfo.get("profile_image").toString();

		try {
			UserDTO user = new UserDTO();
			user.setUid(uid);
			user.setUsername(nickname);
			user.setProfileImage(profile_image);
			
			//이미 가입된 회원
			if (userservice.uidDuplicateCheck(uid)) {
				
				resultMap.put("status", true);
				status = HttpStatus.ACCEPTED;
				System.out.println("login");

			} else {
			//가입 안된 사용자 카카오 계정으로 회원가입
				resultMap.put("status", false);
				resultMap.put("log", "회원가입이 필요합니다.");
				status = HttpStatus.ACCEPTED;

			}

			res.setHeader("access-token", access_token);
			resultMap.put("access-token", access_token);

		} catch (RuntimeException e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	@PostMapping("/user/signup")
	@ApiOperation(value = "회원가입하기")
	public ResponseEntity<Map<String, Object>> signup(@RequestBody UserDTO user, HttpServletRequest req) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		HttpStatus status = null;

		String access_token = req.getHeader("access_token");

		HashMap<String, Object> userInfo = kakaoservice.getUserInfo(access_token);

		long uid = (long) userInfo.get("id");
		String nickname = userInfo.get("nickname").toString();
		String profile_image = userInfo.get("profile_image").toString();

		user.setUid(uid);
		user.setUsername(nickname);
		user.setProfileImage(profile_image);

		Calendar cal = Calendar.getInstance();

		int year = cal.get(cal.YEAR);

		int ages = ((year - user.getYear() + 1) / 10) * 10;
		user.setAges(ages);

		List<Integer> interest_category = user.getInterestCategory();

		try {
			userservice.signUp(interest_category, user);

			resultMap.put("status", "success");
			resultMap.put("info", user);
			status = HttpStatus.ACCEPTED;

		} catch (RuntimeException e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	@DeleteMapping("/user")
	@ApiOperation(value = "회원 탈퇴하기")
	public ResponseEntity<Map<String, Object>> deleteUserInfo(HttpServletRequest req) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		HttpStatus status = null;

		String access_token = req.getHeader("access_token");

		HashMap<String, Object> userInfo = kakaoservice.getUserInfo(access_token);

		long uid = (long) userInfo.get("id");
		
		try {
			kakaoservice.deleteUserInfo(access_token);
			
			userservice.deleteUserInfo(uid);
			
			resultMap.put("status", "success");
			status = HttpStatus.ACCEPTED;

		} catch (RuntimeException e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		
		
		return new ResponseEntity<Map<String, Object>>(resultMap, status);

	}

	@PostMapping("/user/survey")
	@ApiOperation(value = "성향 설문조사하기")
	public ResponseEntity<Map<String, Object>> survey(@RequestBody SurveyDTO survey, HttpServletRequest req) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		HttpStatus status = null;

		String access_token = req.getHeader("access_token");

		HashMap<String, Object> userInfo = kakaoservice.getUserInfo(access_token);

		long uid = (long) userInfo.get("id");

		try {
			survey = userservice.survey(survey, uid);

			resultMap.put("status", true);
			resultMap.put("info", survey);
			status = HttpStatus.ACCEPTED;

		} catch (RuntimeException e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	@GetMapping("/area/{area}")
	@ApiOperation(value = "지역정보 가져오기")
	public List<String> area(@PathVariable String area) {
		return userservice.area(area);
	}

	@GetMapping("/user/info")
	@ApiOperation(value = "회원정보 가져오기")
	public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest req) {
		logger.info("--------------------User Information-----------------------------");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		HttpStatus status = null;

		String access_token = req.getHeader("access_token");

		HashMap<String, Object> userInfo = kakaoservice.getUserInfo(access_token);

		long uid = (long) userInfo.get("id");

		try {

			Map<String, Object> userinfo = new HashMap<String, Object>();
			userinfo = userservice.getUserInfo(uid);

			resultMap.put("status", "success");
			resultMap.put("info", userinfo);
			status = HttpStatus.ACCEPTED;

		} catch (RuntimeException e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	@PutMapping("/user/update")
	@ApiOperation(value = "회원정보 수정하기")
	public ResponseEntity<Map<String, Object>> updateUserInfo(@RequestBody UserDTO user, HttpServletRequest req) {
		logger.info("--------------------Update User Information-----------------------------");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HttpStatus status = null;

		String access_token = req.getHeader("access_token");

		HashMap<String, Object> userInfo = kakaoservice.getUserInfo(access_token);

		long uid = (long) userInfo.get("id");
		String nickname = userInfo.get("nickname").toString();
		String profile_image = userInfo.get("profile_image").toString();

		user.setUid(uid);
		user.setUsername(nickname);
		user.setProfileImage(profile_image);

		Calendar cal = Calendar.getInstance();

		int year = cal.get(cal.YEAR);

		int ages = ((year - user.getYear() + 1) / 10) * 10;
		user.setAges(ages);

		List<Integer> interest_category = user.getInterestCategory();

		try {
			userservice.signUp(interest_category, user);

			resultMap.put("status", "success");
			resultMap.put("info", user);
			status = HttpStatus.ACCEPTED;

		} catch (RuntimeException e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	@PutMapping("/user/survey/update")
	@ApiOperation(value = "설문조사 결과 수정하기")
	public ResponseEntity<Map<String, Object>> updateUserSurvey(@RequestBody SurveyDTO survey, HttpServletRequest req) {
		logger.info("--------------------Update Survey-----------------------------");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		HttpStatus status = null;

		String access_token = req.getHeader("access_token");

		HashMap<String, Object> userInfo = kakaoservice.getUserInfo(access_token);

		long uid = (long) userInfo.get("id");

		try {

			survey = userservice.survey(survey, uid);

			resultMap.put("status", true);
			resultMap.put("info", survey);
			status = HttpStatus.ACCEPTED;

		} catch (RuntimeException e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

}
