precision mediump float;

uniform sampler2D sTexture;

varying vec4 ScreenSpaceUV;

vec2 ComputePoints(vec2 p, float i , float j){
    vec2 local;
    if(i==0.0){
       // local.x = (21.0/16.0)*p.x;
       local.x = 1.627*p.x;
    }else if(i>=1.0&&i<=2.0){
        //local.x = (13.0/8.0)*p.x - (i/8.0+1.0/16.0);
        local.x = 0.8781*p.x + 0.21246;
    }else if(i>=2.0){
        //local.x = (21.0/16.0)*p.x - (5.0/16.0);
        local.x = 1.627*p.x - 0.627;
    }


     if(j==0.0){
            //local.y = (21.0/16.0)*p.y;
            local.y = 1.627*p.y;
     }else if(j>=1.0&&j<=2.0){
            //local.y =  (13.0/8.0)*p.y - (j/8.0+1.0/16.0);
            local.y = 0.8781*p.y + 0.21246;
     }else if(j>=2.0){
            //local.y = (21.0/16.0)*p.y - (5.0/16.0);
            local.y = 1.627*p.y - 0.627;
     }
    return local;
}

vec2 ComputePoints1(vec2 p){
    vec2 local;
    if(p.x<=0.295){
       // local.x = (21.0/16.0)*p.x;
       local.x = 1.627*p.x;
    }else if(p.x>0.295&&p.x<=0.705){
        //local.x = (13.0/8.0)*p.x - (i/8.0+1.0/16.0);
        local.x = 1.6169*p.x - 0.3085;
    }else if(p.x>=0.705){
        //local.x = (21.0/16.0)*p.x - (5.0/16.0);
        local.x = 1.627*p.x - 0.627;
    }

     if(p.y<=0.295){
            //local.y = (21.0/16.0)*p.y;
            local.y = 1.627*p.y;
     }else if(p.y>0.295&&p.y<=0.705){
            //local.y =  (13.0/8.0)*p.y - (j/8.0+1.0/16.0);
            local.y = 1.6169*p.y - 0.3085;
     }else if(p.y>=0.705){
            //local.y = (21.0/16.0)*p.y - (5.0/16.0);
            local.y = 1.627*p.y - 0.627;
     }
    return local;
}



vec2 FillSections(vec2 p){
    vec2 res;

    // Section
    //float i = floor(p.x*3.0);
    //float j = floor(p.y*3.0);

    res = ComputePoints1(p);

    return res;
}

void main()
{
    vec2 uv = ScreenSpaceUV.xy;
#if 1
    bool left_part;
    if(uv.x < 0.5){
        uv.x = 2.0 * uv.x;   // remap [0.0, 0.5] to [0.0, 1.0]
        left_part = true;
    }else{
        uv.x = 2.0 * (uv.x - 0.5);   // remap [0.5, 1.0] to [0.0, 1.0]
        left_part = false;
    }
#endif

    uv = FillSections(uv);

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