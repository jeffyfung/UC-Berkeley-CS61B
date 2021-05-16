package gh2;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHeroLite {
//    public static final double CONCERT_A = 440.0;
//    public static final double CONCERT_C = CONCERT_A * Math.pow(2, 3.0 /
    public static String keyboard;
    public static int key_size;
    public static GuitarString[] gs_array;

    public static void main(String[] args) {
        /* create two guitar strings, for concert A and C */
        keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
        key_size = keyboard.length();
        gs_array = new GuitarString[key_size];

        for (int i = 0; i < key_size; i++) {
            Double tmp_concert = 440.0 * Math.pow(2, (double) (i-24) / 12);
            gs_array[i] = new GuitarString(tmp_concert);
        }

        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int idx = keyboard.indexOf(key);
                if (idx == -1) {
                    continue;
                }
                else {
                    gs_array[idx].pluck();
                }
//                if (key == 'a') {
//                    stringA.pluck();
//                } else if (key == 'c') {
//                    stringC.pluck();
//                }
            }

//            double sample = stringA.sample() + stringC.sample();
            Double sample = 0.0;
            for (int i = 0; i < key_size; i++) {
                /* compute the superposition of samples */
                sample += gs_array[i].sample();
                /* advance the simulation of each guitar string by one step */
                gs_array[i].tic();
            }

            /* play the sample on standard audio */
            StdAudio.play(sample);
        }
    }
}

