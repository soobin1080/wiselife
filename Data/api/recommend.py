from sklearn.decomposition import TruncatedSVD
from scipy.sparse.linalg import svds
from scipy.linalg import svd

import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np
import warnings
import sqlite3
import surprise
import pymysql
warnings.filterwarnings('ignore')

def loaddata():
    # # sqlite3 연결
    # con = sqlite3.connect("db.sqlite3")
    # cur = con.cursor()
    # df_meetings = pd.read_sql('SELECT * FROM api_meeting', con=con)
    # df_reviews = pd.read_sql('SELECT * FROM api_review', con=con)
    # df_reviews.rename(columns={'meeting_id_id':'meeting_id', 'uid_id':'uid'}, inplace=True)
    # con.close()

    # mysql 연결
    conn = pymysql.connect(host='13.125.114.122', user='root', password='wiselife5', db='wiselife', charset='utf8')
    curs = conn.cursor()
    # meeting과 review 데이터 가져오기
    df_meetings = pd.read_sql('select * from meeting', con=conn)
    df_reviews = pd.read_sql('select * from review', con=conn)
    conn.close()

    # meeting와 review를 meeting_id 기준으로 결합하여 ratings에 저장
    meetings = df_meetings[['meeting_id', 'title']]
    reviews = df_reviews[['uid', 'meeting_id', 'score']]
    ratings = pd.merge(meetings, reviews, on='meeting_id')
    return meetings, reviews, ratings

def mbti(uid):
    # mysql 연결
    conn = pymysql.connect(host='13.125.114.122', user='root', password='wiselife5', db='wiselife', charset='utf8')
    curs = conn.cursor()
    # user, survey, usermeeting, meeting, review 데이터 가져오기
    # meetings는 필요한 정보(meeting_id, title)만 가져오기
    df_users = pd.read_sql('select * from user', con=conn)
    df_surveys = pd.read_sql('select * from survey', con=conn)
    df_usermeetings = pd.read_sql('select * from user_meeting', con=conn)
    df_meetings = pd.read_sql('select * from meeting', con=conn)
    df_meetings = df_meetings[['meeting_id', 'title']]
    df_reviews = pd.read_sql('select * from review', con=conn)
    conn.close()
    
    # 미리 계산해둔 big5 평균 점수를 유저별 big5와 비교하여 유저의 mbti 성향을 도출
    # 계산 때문에 column에 넣었던 avg 변수들은 계산 후 제거
    avg = {'openness': 67, 'conscientiousness': 64, 'extraversion': 60, 'agreeableness': 60, 'neuroticism': 55}
    users = pd.merge(df_users, df_surveys, on='uid')
    mbti = []
    users['avg_extraversion'] = avg['extraversion']
    users['avg_openness'] = avg['openness']
    users['avg_agreeableness'] = avg['agreeableness']
    users['avg_conscientiousness'] = avg['conscientiousness']
    for i, user in users.iterrows():
        temp = ''
        if user['extraversion'] < user['avg_extraversion']:
            temp += 'I'
        elif user['extraversion'] >= user['avg_extraversion']:
            temp += 'E'
        if user['openness'] < user['avg_openness']:
            temp += 'S'
        elif user['openness'] >= user['avg_openness']:
            temp += 'N'
        if user['agreeableness'] < user['avg_agreeableness']:
            temp += 'T'
        elif user['agreeableness'] >= user['avg_agreeableness']:
            temp += 'F'
        if user['conscientiousness'] < user['avg_conscientiousness']:
            temp += 'P'
        elif user['conscientiousness'] >= user['avg_conscientiousness']:
            temp += 'J'
        mbti.append(temp)
    users['mbti'] = mbti
    users.drop(['avg_extraversion', 'avg_openness', 'avg_agreeableness', 'avg_conscientiousness'], axis='columns', inplace=True)
    
    # 유저의 mbti를 불러와서 동일한 mbti를 같은 유저 목록을 저장하고 미팅 목록 중 같은 유저들이 참가했던 목록을 계산
    my_mbti = users[users.uid==uid].mbti
    same_users = users[users.mbti==my_mbti.values[0]]
    same_meetings = df_usermeetings[df_usermeetings['uid'].isin(same_users['uid'])]
    recommend_meetings = df_meetings[df_meetings['meeting_id'].isin(same_meetings['meeting_id'])]

    meetings = recommend_meetings[['meeting_id', 'title']]
    reviews = df_reviews[['uid', 'meeting_id', 'score']]
    ratings = pd.merge(meetings, reviews, on='meeting_id')
    return meetings, reviews, ratings

def make_list(uid):
    # 데이터 불러오기
    # mbti 검사결과가 있다면 mbti 실행으로 생긴 결과 값을 받아오고
    # mbti 검사결과가 없거나 전체 미팅 개수가 적거나 리뷰 개수가 적어서 오류가 뜬다면 전체 값을 불러옴
    check = 0
    try:
        meetings, reviews, ratings = mbti(uid)
    except:
        check = 1
    if check == 1 or len(meetings) <= 30 or len(ratings) <= 30:
        meetings, reviews, ratings = loaddata()
    # ratings에서 uid를 인덱스, meeting_id를 컬럼, score를 값으로 하는 피봇을 만들어 저장
    df_user_meeting_ratings = ratings.pivot(
        index='uid',
        columns='meeting_id',
        values='score'
    ).fillna(0) # 결측값은 0으로 대체
    # 피봇 형태로 저장한 ratings 정보를 행렬분해하기 위해 matrix 형태로 변환
    matrix = np.matrix(df_user_meeting_ratings)
    # 각 사용자들이 매기는 점수의 평균을 구해서 user_ratings에 저장
    user_ratings_mean = np.mean(matrix, axis=1)
    # matrix에서 유저 평균 점수를 빼서 저장
    matrix_user_mean = matrix - user_ratings_mean.reshape(-1, 1)
    # 잠재 요인의 크기를 정하기 위해 matrix의 크기가 3보다 크면 3으로 설정하고
    # 작다면 matrix 크기의 최소치로 설정
    if min(matrix_user_mean.shape) > 3:
        num_k = 3
    else:
        num_k = min(matrix_user_mean.shape)-1
    # scipy 패키지의 svds를 활용해서 matrix를 행렬 분해
    U, sigma, Vt = svds(matrix_user_mean, k=num_k)
    # diag를 통해 1차원 행렬인 sigma를 대칭행렬로 변환
    sigma = np.diag(sigma)
    # dot을 통해 잠재요인 결과값까지 포함하여 다시 결합하고 빼줬던 유저 평균을 다시 더함
    svd_user_predicted_ratings = np.dot(np.dot(U, sigma), Vt) + user_ratings_mean.reshape(-1, 1)
    # 결합한 데이터를 df_svd_preds에 저장
    df_svd_preds = pd.DataFrame(svd_user_predicted_ratings, index=df_user_meeting_ratings.index, columns=df_user_meeting_ratings.columns)
    return df_svd_preds, meetings, reviews



def recommend(uid, num_recommendations=12):
    # make_list에서 작업한 정보들 불러오기
    df_svd_preds, meetings, reviews = make_list(uid)
    # uid를 받아서 유저에 따른 추천 목록을 내림차순으로 보여준다
    sorted_user_predictions = df_svd_preds.loc[uid].sort_values(ascending=False)
    # uid를 통해 유저가 작성했던 리뷰들을 불러온다
    user_data = reviews[reviews.uid == uid]
    # 유저가 참여했던 강좌와 리뷰를 meeting_id를 통해 결합하고 score 기준으로 내림차순 정렬
    user_history = user_data.merge(meetings, on='meeting_id').sort_values(['score'], ascending=False)
    # 유저가 가봤던 장소를 제외하고 recommendations 리스트에 저장
    recommendations = meetings[~meetings['meeting_id'].isin(user_history['meeting_id'])]
    # 유저 추천 목록과 유저가 가보지 않았던 장소 목록을 meeting_id로 결합하여 겹치는 것들만 recommendations에 저장
    recommendations = recommendations.merge(pd.DataFrame(sorted_user_predictions).reset_index(), on='meeting_id')
    # recommendations의 컬럼명을 Predictions로 바꾸고 내림차순으로 정렬한뒤 num_recommendations의 개수만큼만 저장
    recommendations = recommendations.rename(columns={uid: 'Predictions'}).sort_values('Predictions', ascending=False).iloc[:num_recommendations, :]

    return recommendations

if __name__ == "__main__":
    # predictions = recommend(1, 10)
    # print(predictions)
    # print(loaddata())
    # print(mbti(1375532981))
    # print(make_list(1375532981))
    # print(mbti(1375532981))
    print(recommend(1375532981))
