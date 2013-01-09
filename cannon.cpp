#include <GLUT/glut.h>
#include <math.h>
#include <stdlib.h>
#define _USE_MATH_DEFINES
#define cameraStartAngleAltitude 90
#define cameraRadiusAltitude 200
float cameraRadiusAzimuth=200;
float cameraStartAngleAzimuth=90;
float cameraCenterx=640;
float cameraCentery=350;
float cameraCenterz=400;
float cameraAngleAltitude=0;
float cameraAngleAzimuth=90;
float cannonElevation=0;
float cannonStep=0;
float cannonRotation=0;


void RotateRight()
{
	cannonRotation--;
}

void RotateLeft()
{
	cannonRotation++;
}

void MoveForward()
{
	cannonStep-=10;
}

void MoveBackward()
{
	cannonStep+=10;
}

void ElevateCannon()
{
	if(cannonElevation>-60)
		cannonElevation--;
}

void DeclineCannon()
{
	if(cannonElevation<0)
		cannonElevation++;
}

void CameraAzimuthHelper()
{
	if(cameraAngleAzimuth>=360+cameraStartAngleAzimuth)
		cameraAngleAzimuth=cameraStartAngleAzimuth;
	//Translate
	cameraCentery-=350;
	cameraCenterx-=640;
	cameraCenterz-=200;
	//Rotate
	cameraCenterx=cameraRadiusAzimuth*cos((cameraAngleAzimuth*M_PI)/180);
	cameraCenterz=cameraRadiusAzimuth*sin((cameraAngleAzimuth*M_PI)/180);
	//Translate
	cameraCentery+=350;
	cameraCenterx+=640;
	cameraCenterz+=200;
}

void InclineCameraAzimuth()
{
	cameraAngleAzimuth++;
	CameraAzimuthHelper();
}

void DeclineCameraAzimuth()
{
	cameraAngleAzimuth--;
	CameraAzimuthHelper();
}

void CameraAltitudeHelper()
{
	if(cameraAngleAltitude>=360+cameraStartAngleAltitude)
		cameraAngleAltitude=cameraStartAngleAltitude;

	cameraCentery-=350;
	cameraCenterx-=640;
	cameraCenterz-=200;
	cameraCentery=cameraRadiusAltitude*sin((cameraAngleAltitude*M_PI)/180);
	cameraCenterx=cameraRadiusAltitude*cos((cameraAngleAltitude*M_PI)/180)*cos(cameraAngleAzimuth*M_PI/180);
	cameraCenterz=cameraRadiusAltitude*cos((cameraAngleAltitude*M_PI)/180)*sin(cameraAngleAzimuth*M_PI/180);
	cameraRadiusAzimuth=cameraRadiusAltitude*cos((cameraAngleAltitude*M_PI)/180);
	cameraStartAngleAzimuth=atan(cameraCenterz/cameraCenterx)*180/M_PI;
	cameraCentery+=350;
	cameraCenterx+=640;
	cameraCenterz+=200;
}


void InclineCameraAltitude()
{
	
	cameraAngleAltitude++;
	CameraAltitudeHelper();

}


void DeclineCameraAltitude()
{
	
	cameraAngleAltitude--;
	CameraAltitudeHelper();

}

void DrawBase()
{
	glPushMatrix();
	
	glTranslated(640,400,200);
	glColor3f(1,1,0);
	glScaled(300,30,400);
	glutWireCube(1);

	glPopMatrix();
}

void DrawWheels()
{
	//Wheel1
	glPushMatrix();
	
	glColor3f(0,1,0);
	glTranslated(485,390,320);
	glRotated(90,0,1,0);
	glScaled(30,30,10);
	glutWireTorus(1,0.5,20,20);
	
	glPopMatrix();

	//Wheel2
	glPushMatrix();
	
	glColor3f(0,1,0);
	glTranslated(485,390,80);
	glRotated(90,0,1,0);
	glScaled(30,30,10);
	glutWireTorus(1,0.5,20,20);
	
	glPopMatrix();

	//Wheel3
	glPushMatrix();
	
	glColor3f(0,1,0);
	glTranslated(795,390,320);
	glRotated(90,0,1,0);
	glScaled(30,30,10);
	glutWireTorus(1,0.5,20,20);
	
	glPopMatrix();

	//Wheel4
	glPushMatrix();
	
	glColor3f(0,1,0);
	glTranslated(795,390,80);
	glRotated(90,0,1,0);
	glScaled(30,30,10);
	glutWireTorus(1,0.5,20,20);
	
	glPopMatrix();
}

void DrawHolders()
{
	//Holder1
	glPushMatrix();
	
	glColor3f(0,0,1);
	glTranslated(640+(82.4*cos((10.5-cannonRotation)*M_PI/180)),515,200+(82.4*sin((10.5-cannonRotation)*M_PI/180)));
	glRotated(cannonRotation,0,1,0);
	glScaled(15,140,65);
	glutWireCube(1);
	
	glPopMatrix();

	//Holder2
	glPushMatrix();
	
	glColor3f(0,0,1);
	glTranslated(640-(72.6*cos((12+cannonRotation)*M_PI/180)),515,200+(72.6*sin((12+cannonRotation)*M_PI/180)));
	glRotated(cannonRotation,0,1,0);
	glScaled(15,140,65);
	glutWireCube(1);
	
	glPopMatrix();



	//Sphere1
	glPushMatrix();
	
	glColor3f(0,0,1);
	glTranslated(640+(93.2*cos((9.3-cannonRotation)*M_PI/180)),565,200+(93.2*sin((9.3-cannonRotation)*M_PI/180)));
	glRotated(cannonRotation,0,1,0);
	glScaled(6,6,6);
	glutWireSphere(1,20,20);
	
	glPopMatrix();


	//Sphere2
	glPushMatrix();
	
	glColor3f(0,0,1);
	glTranslated(640-(83.4*cos((10.4+cannonRotation)*M_PI/180)),565,200+(83.4*sin((10.4+cannonRotation)*M_PI/180)));
	glRotated(cannonRotation,0,1,0);
	glScaled(6,6,6);
	glutWireSphere(1,20,20);
	
	glPopMatrix();
}

void DrawHolderBase()
{
	glPushMatrix();
	
	glColor3f(1,0,0);
	glTranslated(640,430,200);
	glRotated(cannonRotation,0,1,0);
	glRotated(90,1,0,0);
	glScaled(85,85,15);
	glutWireTorus(1,0.5,20,20);
	
	glPopMatrix();	
}

void DrawWheelDents()
{
	//Dent1
	glPushMatrix();
	
	glColor3f(0,1,1);
	glTranslated(485,390,320);
	glRotated(-90,0,1,0);
	glScaled(3,3,25);
	glutWireCone(1,2,20,20);
	
	glPopMatrix();

	//Dent2
	glPushMatrix();
	
	glColor3f(0,1,1);
	glTranslated(485,390,80);
	glRotated(-90,0,1,0);
	glScaled(3,3,25);
	glutWireCone(1,2,20,20);
	
	glPopMatrix();

	//Dent3
	glPushMatrix();
	
	glColor3f(0,1,1);
	glTranslated(795,390,320);
	glRotated(90,0,1,0);
	glScaled(3,3,25);
	glutWireCone(1,2,20,20);
	
	glPopMatrix();

	//Dent4
	glPushMatrix();
	
	glColor3f(0,1,1);
	glTranslated(795,390,80);
	glRotated(90,0,1,0);
	glScaled(3,3,25);
	glutWireCone(1,2,20,20);
	
	glPopMatrix();
}

void DrawBody()
{
	//Cone1
	glPushMatrix();
	
	glColor3f(1,0,1);
	glTranslated(640+(50.3*cos((84.3-cannonRotation)*M_PI/180)),575,200+(50.3*sin((84.3-cannonRotation)*M_PI/180)));
	glRotated(cannonRotation,0,1,0);
	glRotated(180,0,1,0);
	glRotated(cannonElevation,1,0,0);
	glScaled(71,71,200);
	glutWireCone(1,1.2,20,20);
	
	glPopMatrix();

	float x=640+(50.3*cos((84.3-cannonRotation)*M_PI/180));
	float z=200+(50.3*sin((84.3-cannonRotation)*M_PI/180));

	//Cone2
	glPushMatrix();
	float x1 = x-(240*cos((-cannonElevation*M_PI)/180)*sin((cannonRotation*M_PI)/180));
	float y1 = 575+(240*sin((-cannonElevation*M_PI)/180)); 
	float z1 = z-(240*cos((-cannonElevation*M_PI)/180)*cos((cannonRotation*M_PI)/180));  
	glColor3f(1,1,0);
	glTranslated(x1,y1,z1);
	glRotated(cannonRotation,0,1,0);
	glRotated(-cannonElevation,1,0,0);
	glScaled(41,41,200);
	glutWireCone(1,0.7,20,20);

	
	glPopMatrix();

	//Sphere1
	glPushMatrix();

	x1 = x+(20*cos((-cannonElevation*M_PI)/180)*sin((cannonRotation*M_PI)/180));
	y1 = 575-(20*sin((-cannonElevation*M_PI)/180)); 
	z1 = z+(20*cos((-cannonElevation*M_PI)/180)*cos((cannonRotation*M_PI)/180));  

	glColor3f(1,0,1);
	glTranslated(x1, y1, z1);
	glRotated(cannonRotation,0,1,0);
	glRotated(-cannonElevation,1,0,0);
	glScaled(73,73,73);
	glutWireSphere(1,20,20);
	
	glPopMatrix();

	//Ring
	glPushMatrix();
	
	x1 = x+(20*cos((-cannonElevation*M_PI)/180)*sin((cannonRotation*M_PI)/180));
	y1 = 575-(20*sin((-cannonElevation*M_PI)/180)); 
	z1 = z+(20*cos((-cannonElevation*M_PI)/180)*cos((cannonRotation*M_PI)/180)); 
	glColor3f(1,1,1);
	glTranslated(x1, y1, z1);
	glRotated(cannonRotation,0,1,0);
	glRotated(-cannonElevation,1,0,0);
	glScaled(1,1,1);
	glutWireTorus(5,78,20,20);

	glPopMatrix();

	//Sphere2
	glPushMatrix();
	
	x1 = x+(97*cos((-cannonElevation*M_PI)/180)*sin((cannonRotation*M_PI)/180));
	y1 = 575-(97*sin((-cannonElevation*M_PI)/180)); 
	z1 = z+(97*cos((-cannonElevation*M_PI)/180)*cos((cannonRotation*M_PI)/180)); 
	glColor3f(1,0,1);
	glTranslated(x1, y1, z1);
	glRotated(cannonRotation,0,1,0);
	glRotated(-cannonElevation,1,0,0);
	glScaled(10,10,10);
	glutWireSphere(1,20,20);
	
	glPopMatrix();



}

void DrawCannon()
{
	//Base
	DrawBase();

	//Wheels
	DrawWheels();

	//WheelDents
	DrawWheelDents();

	//HolderBase
	DrawHolderBase();

	//Holders
	DrawHolders();

	//DrawBody
	DrawBody();
}

void Display()
{
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glOrtho(-640,640,-500,500,-2000,2000);
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
	gluLookAt(cameraCenterx,cameraCentery,cameraCenterz,640,350,200,0,1,0);


	glClear(GL_COLOR_BUFFER_BIT);

	glPushMatrix();

	glTranslated(0,0,cannonStep);
	DrawCannon();

	glPopMatrix();

	glFlush();
}
void MyKeyboard(int thekey, int mouseX, int mouseY)
{
	switch(thekey)
	{
		case GLUT_KEY_UP:InclineCameraAltitude();break;
		case GLUT_KEY_DOWN:DeclineCameraAltitude();break;
		case GLUT_KEY_RIGHT:InclineCameraAzimuth();break;
		case GLUT_KEY_LEFT:DeclineCameraAzimuth();break;
		case 'w':MoveForward();break;
		case 's':MoveBackward();break;
		case 'c':ElevateCannon();break;
		case 'z':DeclineCannon();break;
		case '1':RotateLeft();break;
		case '2':RotateRight();break;

	}
		
}
void anim(int num)
{
	glutPostRedisplay();
	glutTimerFunc(30,anim,0);
}
int main(int argc,char** argr)
{
	glutInit(&argc,argr);
	glutInitDisplayMode(GLUT_SINGLE|GLUT_RGB);
	glutInitWindowSize(1280,900);
	glutInitWindowPosition(50,50);
	glutCreateWindow("Cannon");
	

	glutDisplayFunc(Display);
	glutTimerFunc(30,anim,0);
	glutSpecialFunc(MyKeyboard);


	
	glEnable(GL_DEPTH_TEST);
	glDepthFunc(GL_LEQUAL);
	glEnable(GL_NORMALIZE);
	glClearColor(0.0f,0.0f,0.0f,0.0f);
	glViewport(0,0,1280,900);
	glutMainLoop();
}