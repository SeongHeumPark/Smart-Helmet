#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <unistd.h>
#include <pthread.h>

// 클라이언트 제한 수 : 비정상 종료 시 다시 접속해 기존 소켓과 쓰레드를 제거해야 하므로
// 본래 제한 수인 3의 2배로 잡아둔다.
#define MAXCLIENT		( 6 )

// 버퍼 사이즈
#define BUFF_SIZE		( 10000 )

// 모드 플래그
#define DRIVE			( 1 )
#define CALL			( 2 )

// 데이터 플래그
#define NAVIGATION	( 1 )
#define NOTIFICATION	( 2 )

// 플래그 변수
int mode_flag, data_flag;
int bike_flag = NAVIGATION;

// accept 된 socket 번호를 저장하는 변수
int android;
int odroid;
int bike;

// accept 된 socket 번호를 임시 저장하는 변수
int android_temp;
int odroid_temp;
int bike_temp;

// 클라이언트 수를 체크하는 변수
int clientCounter;

// 각 클라이언트 Thread
void * android_thread();
void * odroid_thread();
void * bike_thread();

int main(void)
{
	struct sockaddr_in servaddr, cliaddr;
	char buffer[BUFF_SIZE] = { 0, };
	int listen_sock, accp_sock;
	int addrlen = sizeof( cliaddr );
	pthread_t android_tid, odroid_tid, bike_tid;
	
	/* 소켓 생성 */
	if( ( listen_sock = socket( PF_INET, SOCK_STREAM, 0 ) ) < 0 ) {
		perror( "socket fail" );
		exit( 0 );
	}
	
	/* 서버 옵션 설정 */
	bzero( (char * ) &servaddr, sizeof( servaddr ) );
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = inet_addr( "192.168.0.100" );
	servaddr.sin_port = htons( 3000 );

	if( setsockopt( listen_sock, SOL_SOCKET, SO_REUSEADDR, ( void * ) &servaddr, sizeof( servaddr ) ) == -1 ) {
		perror( "setsockopt error" );
		exit( 1 );
	}

	/* 연결 수신 대기 */
	if( bind ( listen_sock, ( struct sockaddr * ) &servaddr, sizeof( servaddr ) ) < 0 ) {
		perror( "bind fail" );
		exit( 1 );
	}
	listen( listen_sock, 5 );
	
	puts( "서버가 연결 요청을 기다림" );
	
	/* 연결 처리 */
	while( 1 ) {
		// buffer initialize
		memset( buffer, 0, BUFF_SIZE );
		
		// connection wait
		accp_sock = accept( listen_sock, ( struct sockaddr * ) &cliaddr, ( socklen_t * ) &addrlen );

		if( accp_sock < 0 ) {
			perror( "accept fail" );
			exit( 0 );
		}
		
		printf( "connection client\n" );
		
		// 데이터 읽기
		if( clientCounter <= MAXCLIENT ) {
			read( accp_sock, buffer, BUFF_SIZE );
		}

		// 클라이언트 소켓 배열 지정
		if( strncmp( buffer, "android", 7 ) == 0 ) {
			// display buffer
			printf( "android meesage : %s clinet access\n", buffer );
			
			android = accp_sock;
			
			// 안드로이드 비정상 종료 했을 때 기존에 있던 Thread kill
			if( android_temp == 0 ) {
				android_temp = android;
			}
			else {
				write( android_temp, " ", 1 );
			}
			
			// 클라이언트 제한 수 증가
			++clientCounter;
				
			// 안드로이드 쓰레드 시작	
			if( pthread_create( &android_tid, NULL, ( void * ) android_thread, NULL ) < 0 ) {
				perror( "android thread create error" );
				exit( 1 );
			}
			
			continue;
		}
		else if( strncmp( buffer, "odroid", 6 ) == 0 ) {
			// display buffer
			printf( "%s clinet access\n", buffer );
			
			odroid = accp_sock;
			
			// 오드로이드 비정상 종료 했을 때 기존에 있던 Thread kill
			if( odroid_temp == 0 ) {
				odroid_temp = odroid;
			}
			else {
				write( odroid_temp, " ", 1 );
			}
			
			// 모드를 알려 줌
			if( mode_flag == DRIVE ) {
				write( odroid, "drive", 5 );
			}
			else if( mode_flag == CALL ) {
				write( odroid, "call", 4 );
			}
			
			// 클라이언트 제한 수 증가
			++clientCounter;
			
			// 오드로이드 쓰레드 시작
			if( pthread_create( &odroid_tid, NULL, ( void * ) odroid_thread, NULL ) < 0 ) {
				perror( "android thread create error" );
				exit( 1 );
			}
			
			continue;
		}
		else if( strncmp( buffer, "bike", 4 ) == 0 ) {
			// display buffer
			printf( "%s clinet access\n", buffer );
			
			bike = accp_sock;
			
			// 바이크 비정상 종료 했을 때 기존에 있던 Thread kill
			if( bike_temp == 0 ) {
				bike_temp = bike;
			}
			else {
				write( bike_temp, " ", 1 );
			}
			
			// 클라이언트 제한 수 증가
			++clientCounter;
			
			// bike가 접속되었다고 확인 값을 보냄
			write( bike, "bike", 4 );

			// 바이크 쓰레드 시작
			if( pthread_create( &bike_tid, NULL, ( void * )bike_thread, NULL ) < 0 ) {
				perror( "bike thread create error" );
				exit( 1 );
			}
			
			continue;
		}
	
	}
	
	// server socket close
	close( listen_sock );
	
	return 0;
}

void * android_thread()
{
	char buffer[BUFF_SIZE] = { 0, };
	int read_size = 0;
	
	printf( "android thread start\n" );
	
	while( 1 ) {
		// buffer initialize
		memset( buffer, 0, BUFF_SIZE );
		
		// read data
		read_size = read( android, buffer, BUFF_SIZE );
		
		if( read_size <= 0 ) break;
		
		if( strcmp( buffer, "drive" ) == 0 ) {
			// display buffer
			printf( "android meesage : %s\n", buffer );
			
			// 모드가 변경 됐다고 알림
			write( odroid, buffer, read_size );
			
			// mode flag 변경
			mode_flag = DRIVE;
			
			continue;
		}
		else if( strcmp( buffer, "call" ) == 0 ) {
			// display buffer
			printf( "android meesage : %s\n", buffer );
			
			// 모드가 변경 됐다고 알림
			write( odroid, buffer, read_size );
			
			// mode flag 변경
			mode_flag = CALL;
			
			continue;
		}
		
		if( mode_flag == DRIVE ) {
			if( strcmp( buffer, "noti" ) == 0 ) {
				// display buffer
				printf( "android meesage : %s\n", buffer );
				
				// transfer odroid
				write( odroid, "noti", 4 );
				
				// data flag 변경
				data_flag = NOTIFICATION;
				
				continue;
			}
			else if( strcmp( buffer, "navi" ) == 0 && bike_flag == NAVIGATION ) {
				// display buffer
				printf( "android meesage : %s\n", buffer );
				
				// transfer odroid
				write( odroid, buffer, read_size );
				
				// data_flag를 navigation으로 바꿔줌
				data_flag = NAVIGATION;
				
				continue;
			}

			if( data_flag == NOTIFICATION ) {
				// transfer odroid
				write( odroid, buffer, read_size );

				// data_flag를 navigation으로 바꿔줌
				data_flag = NAVIGATION;
				
				continue;
			}
			else if( data_flag == NAVIGATION && bike_flag == NAVIGATION ) {
				// transfer odroid
				write( odroid, buffer, read_size );

				continue;
			}
		}
			
		if( mode_flag == CALL ) {
			// display buffer
			printf( "android meesage : %s\n", buffer );
				
			if( strcmp( buffer, "left" ) == 0 ) {
				// transfer "0" to bike
				write( bike, "0", 1 );
				
				continue;
			}
			else if( strcmp( buffer, "right" ) == 0 ) {
				// transfer "1" to bike
				write( bike, "1", 1 );
				
				continue;
			}
			else if( strcmp( buffer, "accel" ) == 0 ) {
				// transfer "2" to bike
				write( bike, "2", 1 );
				
				continue;
			}
			else if( strcmp( buffer, "break" ) == 0 ) {
				// transfer "3" to bike
				write( bike, "3", 1 );
				
				continue;
			}
		}
	}
	
	// socket close
	printf( "android client exit\n" );
	close( android );
	
	// client count -1
	--clientCounter;
	
	// 비정상 종료를 위한 임시 저장 변수 초기화
	android_temp = 0;
	
	// flag initialize
	data_flag = 0;
	mode_flag = 0;
	
	// thread close
	printf( "android thread exit\n" );
	pthread_exit( NULL );
}

void * odroid_thread()
{
	char buffer[BUFF_SIZE] = { 0, };
	int read_size = 0;

	printf( "odroid thread start\n" );
	
	while( 1 ) {
		// buffer initialize
		memset( buffer, 0, BUFF_SIZE );

		read_size = read( odroid, buffer, BUFF_SIZE );

		if( read_size <= 0 ) break;
	}
	
	// socket close
	printf( "odroid client exit\n" );
	close( odroid );
	
	// client count -1
	--clientCounter;
	
	// 비정상 종료를 위한 임시 저장 변수 초기화
	odroid_temp = 0;
	
	// 안드로이드가 접속해 있다면 mode_flag를 바꾸지 않는다.
	if( android <= 0 ) mode_flag = 0;
	
	// thread close
	printf( "odroid thread exit\n" );
	pthread_exit( NULL );
}

void * bike_thread()
{
	char buffer[BUFF_SIZE] = { 0, };
	int read_size = 0;

	printf( "bike thread start\n" );
	
	write( bike, "bike", 4 );
	
	while( 1 ) {
		// buffer initialize
		memset( buffer, 0, BUFF_SIZE );

		// read data
		read_size = read( bike, buffer, BUFF_SIZE );

		if( read_size <= 0 ) break;

		if( strcmp( buffer, "S" ) == 0 ) {
			// transfer bike
			write( bike, buffer, read_size );
			
			continue;
		}
		else if( strcmp( buffer, "E" ) == 0 ) {
			printf( "%s\n", buffer );
			
			// transfer bike
			write( bike, buffer, read_size );
			
			break;
		}
		else {
			// interrupt 데이터가 넘어오면 현재의 반대 flag로 변경
			bike_flag = !bike_flag;
			
			// bike_flag 값에 따른 처리
			if( bike_flag == 0 ) {
				printf( "bike message : rear\n" );
				
				write( odroid, "rear", 4 );
			}
			else {
				printf( "bike message : navi\n" );
				
				write( odroid, "navi", 4 );
			}
			
			continue;
		}
	}
	
	// socket close
	printf( "bike client exit\n" );
	close( bike );
	
	// client count -1
	--clientCounter;
	
	// 비정상 종료를 위한 임시 저장 변수 초기화
	bike_temp = 0;
	
	// bike_flag를 NAVIGATION으로 초기화
	bike_flag = NAVIGATION;
	
	// thread close
	printf( "bike thread exit\n" );
	pthread_exit( NULL );
}
