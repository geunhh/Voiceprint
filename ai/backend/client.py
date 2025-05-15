# import asyncio
# import websockets
# import logging
# import json
# import sys

# # 로깅 설정
# logging.basicConfig(level=logging.DEBUG)
# logger = logging.getLogger(__name__)


# async def keep_alive_websocket_client():
#     # URL에 쿼리 파라미터 추가
#     client_id = 1
#     uri = f"ws://localhost:8000/ws?user_id={client_id}"
    
#     print(f"웹소켓 서버에 연결 시도... (client_id: {client_id})")
#     print("종료하려면 'exit' 또는 'quit'를 입력하세요.")
    
#     try:
#         async with websockets.connect(uri) as websocket:
#             print("웹소켓 서버에 연결됨")
            
#             # 초기 메시지 전송
#             await websocket.send("안녕하세요!")
#             print("메시지 전송: 안녕하세요!")
            
#             # 응답 대기
#             response = await websocket.recv()
#             print(f"서버 응답: {response}")
            
#             # 메시지 송수신 루프와 사용자 입력 처리를 위한 태스크 생성
#             async def user_input():
#                 while True:
#                     message = await asyncio.get_event_loop().run_in_executor(
#                         None, input, "메시지 입력: "
#                     )
                    
#                     if message.lower() in ["exit", "quit"]:
#                         print("클라이언트를 종료합니다...")
#                         break
                    
#                     await websocket.send(message)
#                     print(f"메시지 전송: {message}")
            
#             async def receive_messages():
#                 while True:
#                     try:
#                         response = await websocket.recv()
#                         print(f"서버 응답: {response}")
#                     except websockets.exceptions.ConnectionClosed:
#                         print("서버와의 연결이 종료되었습니다.")
#                         break
            
#             # 두 태스크를 동시에 실행
#             input_task = asyncio.create_task(user_input())
#             receive_task = asyncio.create_task(receive_messages())
            
#             # 어느 한 태스크가 완료될 때까지 대기
#             done, pending = await asyncio.wait(
#                 [input_task, receive_task],
#                 return_when=asyncio.FIRST_COMPLETED
#             )
            
#             # 남은 태스크 취소
#             for task in pending:
#                 task.cancel()
            
#     except Exception as e:
#         print(f"오류 발생: {e}")
    
#     print("연결이 종료되었습니다.")

# if __name__ == "__main__":
#     try:
#         asyncio.run(keep_alive_websocket_client())
#     except KeyboardInterrupt:
#         print("\n사용자에 의해 프로그램이 종료되었습니다.")
#     except Exception as e:
#         print(f"예상치 못한 오류: {e}")