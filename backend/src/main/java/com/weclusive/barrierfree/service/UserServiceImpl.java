package com.weclusive.barrierfree.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.weclusive.barrierfree.dto.Email;
import com.weclusive.barrierfree.dto.Impairment;
import com.weclusive.barrierfree.dto.UserJoin;
import com.weclusive.barrierfree.dto.UserJoinKakao;
import com.weclusive.barrierfree.dto.UserLoginDto;
import com.weclusive.barrierfree.entity.User;
import com.weclusive.barrierfree.entity.UserImpairment;
import com.weclusive.barrierfree.repository.TokenRepository;
import com.weclusive.barrierfree.repository.UserImpairmentRepository;
import com.weclusive.barrierfree.repository.UserRepository;
import com.weclusive.barrierfree.util.JwtUtil;
import com.weclusive.barrierfree.util.MailContentBuilder;
import com.weclusive.barrierfree.util.StringUtils;
import com.weclusive.barrierfree.util.TimeUtils;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserImpairmentRepository userImpairmentRepository;

	@Autowired
	private TokenRepository tokenRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MailServiceImpl mailService;

	@Autowired
	private MailContentBuilder mailContentBuilder;
 
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private AuthenticationManager authenticationManager;

	// ?????? ??????
	@Override
	public void registUser(UserJoin userJoin) {
		String now = TimeUtils.curTime(); // ?????? ??????
		userRepository.save(
				User.builder()
				.userId(userJoin.getUserId())
				.userNickname(userJoin.getUserNickname())
				.userEmail(userJoin.getUserEmail())
				.userPwd(passwordEncoder.encode(userJoin.getUserPwd())) // ???????????? ?????????
				.regDt(now).regId(userJoin.getUserId())
				.modDt(now).modId(userJoin.getUserId())
				.enabledYn('n')
				.certKey(mailService.generate_key()) // ????????? ?????? ?????? ???
				.build());

		User user = findByUserId(userJoin.getUserId());
		String userId = user.getUserId();
		int userSeq = user.getUserSeq();

		if (userJoin.getPhysical() == 1) { // ????????????
			saveImpairment(userSeq, userId, "physical", now);
		}
		if (userJoin.getVisibility() == 1) { // ????????????
			saveImpairment(userSeq, userId, "visibility", now);
		}
		if (userJoin.getDeaf() == 1) { // ????????????
			saveImpairment(userSeq, userId, "deaf", now);
		}
		if (userJoin.getInfant() == 1) { // ???????????????
			saveImpairment(userSeq, userId, "infant", now);
		}
		if (userJoin.getSenior() == 1) { // ?????????
			saveImpairment(userSeq, userId, "senior", now);
		}
	}

	// ????????? ?????? ??????
	@Override
	public void registKakaoUser(UserJoinKakao userJoinKakao, String userEmail) {
		String now = TimeUtils.curTime(); // ?????? ??????

		userRepository.save(
				User.builder()
				.userId(userJoinKakao.getUserId())
				.userEmail(userEmail)
				.userNickname(userJoinKakao.getUserNickname())
				.userPwd(passwordEncoder.encode(userEmail)) // ???????????? ?????????
				.regDt(now).regId(userJoinKakao.getUserId())
				.modDt(now).modId(userJoinKakao.getUserId())
				.certKey(null)
				.enabledYn('y') // ????????? ????????? ?????? ????????? ?????? ??????
				.build());

		User user = findByUserId(userJoinKakao.getUserId());
		String userId = user.getUserId();
		int userSeq = user.getUserSeq();

		if (userJoinKakao.getPhysical() == 1) { // ????????????
			saveImpairment(userSeq, userId, "physical", now);
		}
		if (userJoinKakao.getVisibility() == 1) { // ????????????
			saveImpairment(userSeq, userId, "visibility", now);
		}
		if (userJoinKakao.getDeaf() == 1) { // ????????????
			saveImpairment(userSeq, userId, "deaf", now);
		}
		if (userJoinKakao.getInfant() == 1) { // ???????????????
			saveImpairment(userSeq, userId, "infant", now);
		}
		if (userJoinKakao.getSenior() == 1) { // ?????????
			saveImpairment(userSeq, userId, "senior", now);
		}

	}

	// ????????? certKey??? ??? ????????? ????????? ???????????? ?????????.
	@Override
	public void sendEmailwithUserKey(String email, String id) {
		User user = userRepository.findByUserId(id);
		String link = "https://barrierfree.cf/user/email/certified?userNickname=" + user.getUserNickname()
				+ "&certified=" + user.getCertKey();
		String message = mailContentBuilder.build(link);
		try {
			mailService.sendMail(new Email(email, id, "[BarrierFree] ????????? ??????", message));
		} catch (MailException e) {
			e.printStackTrace();
		}
	}

	// ????????? ??????????????? ??????
	@Override
	public User email_cert_check(String userNickname) {
		User user = userRepository.findByUserNickname(userNickname);
		return user;
	}
 
	// ????????? ????????? ??????, ????????? ?????? ????????? 'n' -> 'y' ???????????? ??????
	@Override
	public void email_certified_update(User user) {
		user.setEnabledYn('y');
		userRepository.save(user);
	}

	// ????????? ????????? ??? ??????????????? ????????? ??? ???????????????
	// ???????????? true ??????
	// ??????????????? false ??????
	@Override
	public boolean encodePassword(UserLoginDto loginUser) {
		User user = userRepository.findByUserId(loginUser.getUserId());

		if (!passwordEncoder.matches(loginUser.getUserPwd(), user.getUserPwd()))
			return false;

		return true;
	}

//	// refresh token ?????? ??? DB??? ??????
//	@Override
//	public String createRefreshToken(User user) {
//		String ref_token = jwtUtil.generateRefreshToken((user.getUserId()));
//		tokenRepository.save(Token.builder().userSeq(user.getUserSeq()).tokenRefTK(ref_token).build());
//		return ref_token;
//	}

	// AccessToken ??????
	@Override
	public String createAccessToken(User user) {
		try {
			authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(user.getUserId(), user.getUserPwd()));
		} catch (Exception e) {
			System.out.println(e.getMessage()); 
		}
		return jwtUtil.generateAccessToken(user.getUserId());		
	}

	// refreshToken ?????????
	@Transactional
	public User refreshToken(String token, String refreshToken) {
		// ?????? ???????????? ?????? ??????????????? refresh ??? ??? ??????
		return null;
	}

	// ????????? ????????? access token ??????
	@Override
	public String getKakaoAccessToken(String code) throws Exception {
		String access_Token = "";
		String reqURL = "https://kauth.kakao.com/oauth/token";

		try {
			URL url = new URL(reqURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			// POST ????????? ?????? ???????????? false??? setDoOutput??? true???
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);

			// POST ????????? ????????? ???????????? ???????????? ???????????? ?????? ??????
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			StringBuilder sb = new StringBuilder();
			sb.append("grant_type=authorization_code");
			sb.append("&client_id=fa3c898eec92948b420f6f03b934acd1"); // REST_API_KEY ??????
			sb.append("&redirect_uri=https://barrierfree.cf/kakaologinpage"); // ???????????? ?????? redirect_uri ??????
			sb.append("&code=" + code);
			bw.write(sb.toString());
			bw.flush();

			// ?????? ????????? 200????????? ??????
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);

			// ????????? ?????? ?????? JSON????????? Response ????????? ????????????
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String result = "";

			while ((line = br.readLine()) != null) {
				result += line;
			}
			// Gson ?????????????????? ????????? ???????????? JSON?????? ?????? ??????
			JSONParser parser = new JSONParser();
			JSONObject element = (JSONObject) parser.parse(result);

			access_Token = element.get("access_token").toString();

			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return access_Token;
	}

	// ????????? ???????????? ????????? ?????? ??????
	@Override
	public String getKakaoEmail(String token) throws Exception {

		String reqURL = "https://kapi.kakao.com/v2/user/me";

		// access_token??? ???????????? ????????? ?????? ??????
		try {
			URL url = new URL(reqURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Authorization", "Bearer " + token); // ????????? header ??????, access_token??????

			// ?????? ????????? 200????????? ??????
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);

			// ????????? ?????? ?????? JSON????????? Response ????????? ????????????
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String result = "";

			while ((line = br.readLine()) != null) {
				result += line;
			}

			// Gson ?????????????????? JSON??????
			JSONParser parser = new JSONParser();
			JSONObject element = (JSONObject) parser.parse(result);
			JSONObject kakao_account = (JSONObject) element.get("kakao_account");
			
			boolean hasEmail = (boolean) kakao_account.get("has_email");
			String email = "";
			if (hasEmail) {
				email = kakao_account.get("email").toString();
			}

			br.close();

			return email;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// ???????????? ?????? ?????? - ?????? ??????
	@Override
	public User findByUserId(String userId) {
		User user = userRepository.findByUserId(userId);
		return user;
	}

	// ??????????????? ?????? ?????? - ?????? ??????
	@Override
	public User findByUserNickname(String userNickname) {
		User user = userRepository.findByUserNickname(userNickname);
		return user;

	}

	// ???????????? ?????? ?????? - kakao
	@Override
	public User findByUserEmail(String userEmail) {
		User user = userRepository.findByUserEmail(userEmail);
		return user;
	}


	public void saveImpairment(int userSeq, String userId, String code, String now) {
		userImpairmentRepository.save(UserImpairment.builder().userSeq(userSeq).code(code).delYn('n').regDt(now)
				.regId(userId).modDt(now).modId(userId).build());
	}

	// ?????? ???????????? ??????, ????????? ??????
	@Override
	public void sendEmailwithTemp(String userEmail, String userId) {
		String tempPass = StringUtils.getRamdomPassword(10);
		String mail = mailContentBuilder.passBuild(tempPass);
		System.out.println("??????");
		try {
			mailService.sendMail(new Email(userEmail, userId, "[BarrierFree] ?????? ???????????? ??????", mail));
			User user = userRepository.findByUserId(userId);
			user.setUserPwd(passwordEncoder.encode(tempPass));
			userRepository.save(user);
		} catch (MailException e) {
			e.printStackTrace();
		}

	}

	// ????????? ?????? ?????? ????????????
	@Override
	public Impairment readUserImpairment(int userSeq) {
		int returnUserSeq = userRepository.countByDelYnAndUserSeq('n', userSeq);

		if (returnUserSeq != 0) {

			Impairment ui = new Impairment();
			List<String> st = userImpairmentRepository.findImpairment(userSeq);

			for (int i = 0; i < st.size(); i++) { // ?????? ?????? ??? ?????? ??????
				String im = st.get(i);

				switch (im) {
				case "physical":
					ui.setPhysical(1);
					break;
				case "visibility":
					ui.setVisibility(1);
					break;
				case "deaf":
					ui.setDeaf(1);
					break;
				case "infant":
					ui.setInfant(1);
					break;
				case "senior":
					ui.setSenior(1);
					break;
				}
			}
			return ui;
		}
		return null;
	}

	@Override
	public User findByUserSeq(int userSeq) {
		User user = userRepository.findByUserSeq(userSeq);
		return user;
	}

	@Override
	public Map<String, Object> userInfo(String userId) {
		
		Map<String, Object> userinfo = new HashMap<>();
		User user = userRepository.findByUserId(userId);

		if(user != null) {
			userinfo.put("userId", user.getUserId());
			userinfo.put("userNickname", user.getUserNickname());
			userinfo.put("userSeq", user.getUserSeq());
			userinfo.put("userPhoto", user.getUserPhoto());
			userinfo.put("userEmail", user.getUserEmail());
			userinfo.put("impairment", userImpairmentRepository.findImpairment(user.getUserSeq()));
		}
		return userinfo;
	}
	
	@Override
	public boolean modifyUser(User user) throws Exception{
		try {
			User newUser = userRepository.findByUserSeq(user.getUserSeq());
			if(user.getUserNickname()!=null) newUser.setUserNickname(user.getUserNickname());
			if(user.getUserPwd()!=null)  newUser.setUserPwd(passwordEncoder.encode(user.getUserPwd()));
			if(user.getUserPhoto()!=null) newUser.setUserPhoto(user.getUserPhoto());
			newUser.setModDt(TimeUtils.curTime());
			newUser.setModId(userRepository.findByUserSeq(user.getUserSeq()).getUserId());
			
			userRepository.save(newUser);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean withdrawUser(int userSeq) throws Exception {
		try {
			User newUser = userRepository.findByUserSeq(userSeq);
			newUser.setDelYn('y');
			newUser.setModDt(TimeUtils.curTime());
			newUser.setModId(userRepository.findByUserSeq(userSeq).getUserId());
			
			userRepository.save(newUser);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void logoutUser(String accessToken) {
		String userId = jwtUtil.extractUserId(accessToken);
		int userSeq = userRepository.findByUserId(userId).getUserSeq();
		tokenRepository.deleteByUserSeq(userSeq); // ?????? ???????????? ?????? ??????
	}

	@Override
	public int updateUserImpairmentByUserSeq(int userSeq, Impairment impairment) throws Exception {
		// ????????? ????????? ????????? ?????? ?????? ?????? ??????(del_yn = n)
		List<UserImpairment> curImpairment = userImpairmentRepository.findByDelYnAndUserSeq('n', userSeq);
		int res = 0;
		// ????????? ?????? ?????? ???????????? ??????
		// -1) ?????? X, 1) ??????
		// physical, visibility, deaf, infant, senior
		int check[] = new int[] { -1, -1, -1, -1, -1 };

		for (int i = 0; i < curImpairment.size(); i++) { // ?????? ???????????? ?????? ?????? ??? ?????? ??????
			String im = curImpairment.get(i).getCode(); // ???????????? ?????? ?????? ex) physical

			// ????????? ????????? check??? 1???
			switch (im) {
			case "physical":
				check[0] = 1;
				break;
			case "visibility":
				check[1] = 1;
				break;
			case "deaf":
				check[2] = 1;
				break;
			case "infant":
				check[3] = 1;
				break;
			case "senior":
				check[4] = 1;
				break;
			}
		}

		// check : ?????? ?????? ??????(-1) -> impairment : ?????? ?????? ??????(1)
		// ?????? -> ?????? : post_code table??? ????????????
		if (check[0] == -1 && impairment.getPhysical() == 1) {
			saveImpairment(0, userSeq);
			res = 1;
		}
		if (check[1] == -1 && impairment.getVisibility() == 1) {
			saveImpairment(1, userSeq);
			res = 1;
		}
		if (check[2] == -1 && impairment.getDeaf() == 1) {
			saveImpairment(2, userSeq);
			res = 1;
		}
		if (check[3] == -1 && impairment.getInfant() == 1) {
			saveImpairment(3, userSeq);
			res = 1;
		}
		if (check[4] == -1 && impairment.getSenior() == 1) {
			saveImpairment(4, userSeq);
			res = 1;
		}

		// check : ?????? ?????? ??????(1) -> impairment : ?????? ?????? ??????(0)
		// ?????? -> ?????? : post_code?????? ???????????? del_yn = y
		if (check[0] == 1 && impairment.getPhysical() == 0) {
			updateImpairment(0, userSeq);
			res = 1;
		}
		if (check[1] == 1 && impairment.getVisibility() == 0) {
			updateImpairment(1, userSeq);
			res = 1;
		}
		if (check[2] == 1 && impairment.getDeaf() == 0) {
			updateImpairment(2, userSeq);
			res = 1;
		}
		if (check[3] == 1 && impairment.getInfant() == 0) {
			updateImpairment(3, userSeq);
			res = 1;
		}
		if (check[4] == 1 && impairment.getSenior() == 0) {
			updateImpairment(4, userSeq);
			res = 1;
		}

		return res;
	}
	
	// ????????? ???????????? ????????????
		public void saveImpairment(int im, int userSeq) {
			String curTime = TimeUtils.curTime();
			String type = "";
			switch (im) {
			case 0:
				type = "physical";
				break;
			case 1:
				type = "visibility";
				break;
			case 2:
				type = "deaf";
				break;
			case 3:
				type = "infant";
				break;
			case 4:
				type = "senior";
				break;
			}

			userImpairmentRepository.save(UserImpairment.builder()
					.userSeq(userSeq)
					.code(type)
					.delYn('n')
					.regDt(curTime)
					.regId(returnUserId(userSeq))
					.modDt(curTime)
					.modId(returnUserId(userSeq)).build());
	}
		
	// userSeq -> userId
	public String returnUserId(int userSeq) {
		Optional<User> list = userRepository.findById(userSeq);
		String userId = list.get().getUserId();
		return userId;
	}
	
	public void updateImpairment(int im, int userSeq) {
		String curTime = TimeUtils.curTime();
		String type = "";
		switch (im) {
		case 0:
			type = "physical";
			break;
		case 1:
			type = "visibility";
			break;
		case 2:
			type = "deaf";
			break;
		case 3:
			type = "infant";
			break;
		case 4:
			type = "senior";
			break;
		}

		Optional<UserImpairment> ui = userImpairmentRepository.findOneByUserSeqCode(userSeq, type);
		ui.get().setDelYn('y');
		ui.get().setModDt(curTime);
		ui.get().setModId(returnUserId(userSeq));
		save(ui.get());
	}
	
	// ????????? ???????????? ????????????
	public UserImpairment save(UserImpairment userImpairment) {
		userImpairmentRepository.save(userImpairment);
		return userImpairment;
	}
}
