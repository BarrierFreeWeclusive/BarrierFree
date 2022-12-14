import React, { useEffect, useState } from 'react';
import axios from 'axios';
import ReviewCardList from './ReviewCardList';
import Button from '../../common/Button';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import './ReviewPage.css';

const ReviewPage = () => {
  const myuser = useSelector((state) => state.user.userData);
  const navigate = useNavigate();
  const [myitemList, mysetItemList] = useState([]);

  const orderbylatest = async () => {
    axios({
      method: 'get',
      url: 'main/recently?userSeq=0',
    })
      .then(function (res) {
        mysetItemList(res.data);
      })
      .catch((error) => {
        // console.log(error)
      });
  };

  const orderbypopular = () => {
    axios({
      url: `/main/scrap?userSeq=0`,
    })
      .then(function (res) {
        mysetItemList(res.data);
      })
      .catch(function () {});
  };
  const orderbypopularweek = () => {
    axios({
      url: `/main/weekscrap?userSeq=0`,
      method: 'get',
    })
      .then(function (res) {
        mysetItemList(res.data);
      })
      .catch(function (error) {});
  };

  const orderbybf = () => {
    if (localStorage) {
      axios({
        url: '/main/follow',
        method: 'get',
        params: {
          userSeq: myuser.userSeq,
        },
      }).then(function (res) {
        mysetItemList(res.data);
        console.log("EEEEEEEEEEE", res.data)
      });
    } else {
      alert('로그인이 필요합니다!');
      navigator('/loginpage');
    }
  };

  useEffect(() => {
    axios({
      method: 'get',
      url: '/main/all?userSeq=0',
    })
      .then(function (res) {
        mysetItemList(res.data);
      })
      .catch((error) => {
        // console.log(error)
      });
  }, []);

  return (
    <div class="box">
      <h1>Review in here</h1>
      <Button order onClick={orderbylatest}>
        최신순
      </Button>
      <Button order onClick={orderbypopular}>
        전체 인기순
      </Button>
      <Button order onClick={orderbypopularweek}>
        이번주 인기순
      </Button>
      <Button order onClick={orderbybf}>
        베프만
      </Button>
      {/* <BasicCardList itemList={myitemList}></BasicCardList> */}
      <ReviewCardList itemList={myitemList}></ReviewCardList>
    </div>
  );
};

export default ReviewPage;
