precision mediump float;

uniform sampler2D sTexture;

varying vec4 ScreenSpaceUV;
vec2 Distort(vec2 p, float factor){

    float theta = atan(p.y, p.x);
    float radius = length(p);

    float a = pow(2.0/3.0, 1.0/3.0);

    float b = 9.0*pow(factor, 2.0)*radius + sqrt(3.0)*sqrt(27.0*pow(factor, 4.0)*pow(radius, 2.0)+4.0*pow(factor, 3.0));

    float c = pow(b, 1.0/3.0)/(pow(2.0, 1.0/3.0)*pow(3.0, 2.0/3.0)*factor);

    radius = c-a/pow(b, 1.0/3.0);

    p.x = radius*cos(theta);
    p.y = radius*sin(theta);

    return 0.5*(p+1.0);
}



void main()
{
//   vec2 uv = 2.0*ScreenSpaceUV.xy -1.0;
   vec2 uv = ScreenSpaceUV.zw;

#if 1
   bool left_part;
   if(uv.x < 0.0){
       uv.x = 2.0 * uv.x + 1.0;   // remap [-1.0, 0.0] to [-1.0, 1.0]
       left_part = true;
   }else{
       uv.x = 2.0 * (uv.x - 0.5);   // remap [0.0, 1.0] to [-1.0, 1.0]
       left_part = false;
   }
#endif
   uv = Distort(uv, 1.0);
#if 1
   if(left_part){
       uv.x = 0.5 * uv.x;
   }else{
       uv.x = 0.5 * uv.x + 0.5;
   }
#endif

   vec4 c  = texture2D(sTexture, uv);

   gl_FragColor = c;
}