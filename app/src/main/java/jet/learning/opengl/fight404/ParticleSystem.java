package jet.learning.opengl.fight404;

import java.nio.ByteBuffer;

/**
 * Created by mazhen'gui on 2017/12/20.
 */

final class ParticleSystem {
    TransformFeedbackObject[] particle_chains = new TransformFeedbackObject[2];
    int emitter_count = Fireworks.MAX_EMITTER_COUNT/2;
    int emitter_source_buffer;
    ByteBuffer emitter_buffer;
    long last_update_time;

    int current_chain;
    boolean first_loop;

    int count = 0;
    int random_texture_1d;
    int particle_sprite;
    int nebula_sprite;
}
