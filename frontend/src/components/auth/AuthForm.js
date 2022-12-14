import styled from 'styled-components';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../common/Button';
import palette from '../../lib/styles/palette';
import Physical from '../images/PhysicalCheck.png';
import PhysicalHide from '../images/Physical60.png';
import Auditory from '../images/AuditoryCheck.png';
import AuditoryHide from '../images/Auditory60.png';
import Pregnant from '../images/PregnantCheck.png';
import PregnantHide from '../images/Pregnant60.png';
import Senior from '../images/SeniorCheck.png';
import SeniorHide from '../images/Senior60.png';
import Visual from '../images/VisualCheck.png';
import VisualHide from '../images/Visual60.png';
import axios from 'axios';
import KakaoImage from '../images/kakao_login_large_wide.png';
import TextField from '@material-ui/core/TextField';

const API_KEY = process.env.REACT_APP_KAKAO_API_KEY;
const KAKAO_LOGIN_URL =
  'https://kauth.kakao.com/oauth/authorize?client_id=' +
  API_KEY +
  '&redirect_uri=https://barrierfree.cf/kakaologinpage&response_type=code';

const AuthFormBlock = styled.div`
  h2 {
    margin: 0;
    margin-bottom: 1rem;
  }
  p {
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
  }
`;

const AuthBarrierIconBlock = styled.div`
  img {
    margin: 1.5rem 0.75rem 0;
    cursor: pointer;
  }
  img.active {
    border: 2px solid red;
  }
`;

const StyledInput = styled.input`
  font-size: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  border: none;
  border-bottom: 1px solid black;
  padding-bottom: 0.5rem;
  outline: none;
  width: 95%;
  background-color: none;

  &:focus {
    color: $oc-teal-7;
    border-bottom: 1px solid ${palette.pink[0]};
  }

  & + & {
    margin-top: 1.5rem;
  }
`;
const ButtonWithMarginTop = styled(Button)`
  margin-top: 1.5rem;
  width: 90%;
  padding-right: 10px;
`;

const ButtonWith = styled(Button)`
  margin-top: 0.5rem;
  width: 90%;
  padding-right: 10px;
`;

const KakaoJoinButton = styled.button`
  background-color: transparent;
  border-color: transparent;
`;

const textMap = {
  login: '?????????',
  register: '????????????',
  registerkakao: '?????????????????????',
};

const AuthForm = ({
  pwdCfm,
  form,
  type,
  onChange,
  onSubmit,
  setForm,
  loading,
  // onLogin,
}) => {
  const onCheckId = async () => {
    // console.log(form.userId.trim().length);
    const idlength = form.userId.trim().length;
    // console.log(typeof idlength);
    if (idlength >= 5 && idlength <= 20) {
      try {
        const response = await axios({
          url: '/user/check/id',
          method: 'post',
          params: { userId: form.userId },
        });
        // console.log(response);
        if (response.data == 'success') {
          alert('?????? ????????? ??????????????????!????');
          setForm({ ...form, EnableuserId: true });
          // console.log("??????!");
          // console.log(form);
        } else if (response.data == 'fail') {
          alert('???????????? ???????????? ??????????????????!????');
          setForm({ ...form, EnableuserId: false });
        }
      } catch (e) {
        alert('????????? ???????????????!????');
        // alert('???????????? ???????????? ??????????????????!????');
        // setForm({ ...form, EnableuserId: false });
        // console.log(e);
        // console.log(form);
      }
    } else if (idlength === 0) {
      alert('??? ?????? ?????????????????????????');
      // console.log('blank');
    } else {
      alert('5~20?????? ??????????????????!');
    }
  };
  const onCheckNickname = async (event) => {
    if (form.userNickname.trim()) {
      try {
        const response = await axios({
          url: '/user/check/nickname',
          method: 'get',
          params: { userNickname: form.userNickname },
        });
        // console.log(response);
        if (response.data == 'success') {
          setForm({ ...form, EnableuserNickname: true });
          alert('?????? ????????? ??????????????????!????');
          // console.log('??????!');
        } else if (response.data == 'fail') {
          setForm({ ...form, EnableuserNickname: false });
          alert('???????????? ???????????? ??????????????????!????');
        }
      } catch (e) {
        alert('????????? ???????????????!????');
      }
    } else {
      alert('??? ?????? ?????????????????????????');
      // console.log('blank');
    }
  };
  const text = textMap[type];

  const [barrierIcon, setBarrierIcon] = useState({
    physicalFlag: false,
    visibilityFlag: false,
    deafFlag: false,
    infantFlag: false,
    seniorFlag: false,
  });
  const { physicalFlag, visibilityFlag, deafFlag, infantFlag, seniorFlag } =
    barrierIcon;

  return (
    <AuthFormBlock>
      <h1>
        <span style={{ color: '#EA5455' }}>???</span>??????{' '}
        <span style={{ color: '#EA5455' }}>???</span>?????? <br />
        ?????? ?????? ???????????????!
      </h1>
      {/* {(type === 'register' || type === 'registerkakao') && <h4>|????????????|</h4>} */}

      <form onSubmit={onSubmit}>
        <div>
          {type !== 'login' && (
            <TextField
              style={{ width: 210 }}
              label="?????????"
              variant="standard"
              name="userId"
              placeholder="???????????? ???????????????"
              onChange={onChange}
              value={form.userId}
            />
          )}
          {type === 'login' && (
            <TextField
              style={{ width: 300 }}
              label="?????????"
              variant="standard"
              name="userId"
              placeholder="???????????? ???????????????"
              onChange={onChange}
              value={form.userId}
            />
          )}
          {type !== 'login' && (
            <Button
              type="button"
              onClick={onCheckId}
              style={{ height: 30, marginTop: '0.3rem' }}
            >
              ?????? ??????
            </Button>
          )}
        </div>
        {type === 'register' && (
          <TextField
            label="?????????"
            style={{ width: 300 }}
            variant="standard"
            name="userEmail"
            placeholder="???????????? ???????????????"
            type="email"
            onChange={onChange}
            value={form.userEmail}
          />
        )}
        {type !== 'registerkakao' && (
          <div>
            <TextField
              variant="standard"
              label="????????????"
              name="userPwd"
              type="password"
              placeholder="??????????????? ???????????????"
              onChange={onChange}
              style={{ width: 300 }}
              // value={form.userPwd}
            />
          </div>
        )}
        {type === 'register' && (
          <TextField
            variant="standard"
            label="???????????? ??????"
            style={{ width: 300 }}
            name="userPwdCfm"
            type="password"
            placeholder="??????????????? ?????? ??? ???????????????"
            onChange={onChange}
            value={form.userPwdCfm}
          />
        )}
        {pwdCfm === false && (
          <p style={{ color: 'red' }}>??????????????? ??????????????????</p>
        )}
        {(type === 'register' || type === 'registerkakao') && (
          <TextField
            style={{ width: 210 }}
            label="?????????"
            variant="standard"
            name="userNickname"
            placeholder="???????????? ???????????????"
            onChange={onChange}
          />
        )}
        {type !== 'login' && (
          <Button
            type="button"
            onClick={onCheckNickname}
            style={{ height: 30, marginTop: '0.3rem' }}
          >
            ?????? ??????
          </Button>
        )}
        {(type === 'register' || type === 'registerkakao') && (
          <AuthBarrierIconBlock>
            <div align="center" className="barriericon">
              <img
                name="physical"
                src={physicalFlag ? Physical : PhysicalHide}
                width="35"
                onClick={() => {
                  if (form.physical) {
                    setForm({ ...form, physical: 0 });
                    setBarrierIcon({ ...barrierIcon, physicalFlag: false });
                  } else {
                    setForm({ ...form, physical: 1 });
                    setBarrierIcon({ ...barrierIcon, physicalFlag: true });
                  }
                }}
              ></img>
              <img
                name="visibility"
                src={visibilityFlag ? Visual : VisualHide}
                width="35"
                onClick={() => {
                  if (form.visibility) {
                    setForm({ ...form, visibility: 0 });
                    setBarrierIcon({ ...barrierIcon, visibilityFlag: false });
                  } else {
                    setForm({ ...form, visibility: 1 });
                    setBarrierIcon({ ...barrierIcon, visibilityFlag: true });
                  }
                }}
              ></img>
              <img
                name="deaf"
                src={deafFlag ? Auditory : AuditoryHide}
                width="35"
                onClick={() => {
                  if (form.deaf) {
                    setForm({ ...form, deaf: 0 });
                    setBarrierIcon({ ...barrierIcon, deafFlag: false });
                  } else {
                    setForm({ ...form, deaf: 1 });
                    setBarrierIcon({ ...barrierIcon, deafFlag: true });
                  }
                }}
              ></img>
              <img
                name="infant"
                src={infantFlag ? Pregnant : PregnantHide}
                width="35"
                onClick={() => {
                  if (form.infant) {
                    setForm({ ...form, infant: 0 });
                    setBarrierIcon({ ...barrierIcon, infantFlag: false });
                  } else {
                    setForm({ ...form, infant: 1 });
                    setBarrierIcon({ ...barrierIcon, infantFlag: true });
                  }
                }}
              ></img>
              <img
                name="senior"
                src={seniorFlag ? Senior : SeniorHide}
                width="35"
                onClick={() => {
                  if (form.senior) {
                    setForm({ ...form, senior: 0 });
                    setBarrierIcon({ ...barrierIcon, seniorFlag: false });
                  } else {
                    setForm({ ...form, senior: 1 });
                    setBarrierIcon({ ...barrierIcon, seniorFlag: true });
                  }
                }}
              ></img>
            </div>
          </AuthBarrierIconBlock>
        )}
        {loading === true && type === 'login' && <h4>???????????? ??????????????????</h4>}
        {loading === true && type === 'register' && (
          <h4>??????????????? ??????????????????</h4>
        )}
        {type === 'register' && (
          <ButtonWithMarginTop type="submit" cyan fullWidth>
            ????????????
          </ButtonWithMarginTop>
        )}
        {type === 'login' && (
          <ButtonWith type="submit" cyan fullWidth>
            ?????????
          </ButtonWith>
        )}
        {type === 'registerkakao' && (
          <KakaoJoinButton type="submit">
            <img src={KakaoImage} width="350px" />
          </KakaoJoinButton>
        )}
      </form>
      {type === 'login' && (
        <a href={KAKAO_LOGIN_URL}>
          <img src={KakaoImage} width="350px" />
        </a>
      )}
      {type === 'register' && (
        <a href={KAKAO_LOGIN_URL}>
          <img src={KakaoImage} width="350px" />
        </a>
      )}
      {type === 'login' && (
        <Link to="/registerpage">
          <ButtonWith cyan fullWidth>
            ????????????
          </ButtonWith>
        </Link>
      )}
      {/* {type === 'kakaoOauth' && (
        <div>
          ????????? ????????? ?????? ????????????. <br />
          ????????? ????????? ?????????.
        </div>
      )} */}
    </AuthFormBlock>
  );
};

export default AuthForm;
