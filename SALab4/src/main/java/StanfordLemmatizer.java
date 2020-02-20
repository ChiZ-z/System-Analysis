import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.IntStream;

public class StanfordLemmatizer {


    private StanfordCoreNLP pipeline;
    private final static int N = 60;
    private final static int NFolders = 3;
    private static String[] folders = {"atheism", "medicine", "space"};
    private final static Color first = new Color(0.9f, 0.001f, 0.001f);
    private final static Color first2 = new Color(0.7f, 0.001f, 0.001f);
    private final static Color first3 = new Color(0.5f, 0.001f, 0.001f);
    private final static Color second = new Color(0.001f, 0.9f, 0.001f);
    private final static Color second2 = new Color(0.001f, 0.7f, 0.001f);
    private final static Color second3 = new Color(0.001f, 0.5f, 0.001f);
    private final static Color third = new Color(0.001f, 0.001f, 0.9f);
    private final static Color third2 = new Color(0.001f, 0.001f, 0.7f);
    private final static Color third3 = new Color(0.001f, 0.001f, 0.5f);

    public StanfordLemmatizer() {
        // Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        /*
         * This is a pipeline that takes in a string and returns various analyzed linguistic forms.
         * The String is tokenized via a tokenizer (such as PTBTokenizerAnnotator),
         * and then other sequence model style annotation can be used to add things like lemmas,
         * POS tags, and named entities. These are returned as a list of CoreLabels.
         * Other analysis components build and store parse trees, dependency graphs, etc.
         *
         * This class is designed to apply multiple Annotators to an Annotation.
         * The idea is that you first build up the pipeline by adding Annotators,
         * and then you take the objects you wish to annotate and pass them in and
         * get in return a fully annotated object.
         *
         *  StanfordCoreNLP loads a lot of models, so you probably
         *  only want to do this once per execution
         */
        this.pipeline = new StanfordCoreNLP(props);
    }

    public List<String> lemmatize(String documentText) {
        List<String> lemmas = new LinkedList<String>();
        // Create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);
        // run all Annotators on this text
        this.pipeline.annotate(document);
        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
                lemmas.add(token.get(LemmaAnnotation.class));
            }
        }
        return lemmas;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static Map<String, Integer> subMapByInteger(Map<String, Integer> map) {
        Map<String, Integer> result = new HashMap<>();
        int count = 0;
        int max = 0;
        List<String> deleteList = new ArrayList<>();
        List<String> stopDelete = new ArrayList<>();
        do {
            for (String str : map.keySet()) {
                if (map.get(str) >= max) {
                    max = map.get(str);
                }
            }
            for (String str : map.keySet()) {
                if (map.get(str) == max) {
                    if (str.length() < 3) {
                        stopDelete.add(str);
                        break;
                    }
                    deleteList.add(str);
                    count++;
                    if (count == N)
                        break;
                }
            }
            for (String del : stopDelete) {
                map.keySet().removeIf(s -> s.equals(del));
            }
            for (String del : deleteList) {
                for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); ) {
                    if (it.next().equals(del)) {
                        result.put(del, map.get(del));
                        it.remove();
                    }
                }
            }
            max = 0;
        } while (count < N);
        return result;
    }


    public static void main(String[] args) throws IOException, InvalidDataException {
        StanfordLemmatizer stanfordLemmatizer = new StanfordLemmatizer();
        System.out.println("Starting Stanford Lemmatizer");
        Map<String, Map<String, Integer>> tezaurus = new HashMap<>();
        Map<String, Integer> map = new HashMap<>();
        Map<Integer, String> texts = new HashMap<>();
        for (int i = 0, index = 0; i < NFolders; i++) {
            File directory = new File(folders[i]);
            File[] contents = directory.listFiles();
            for (int j = 0; j < 4; j++) {
                String text = new String(Files.readAllBytes(Objects.requireNonNull(contents)[j].toPath()));
                String output = ExudeData.getInstance().filterStoppingsKeepDuplicates(text);
                Map<String, Integer> temp = new HashMap<>();
                StringBuilder resultLemmStop = new StringBuilder();
                for (String str : stanfordLemmatizer.lemmatize(output)) {
                    resultLemmStop.append(" ").append(str);
                    if (temp.containsKey(str)) {
                        temp.put(str, temp.get(str) + 1);
                    } else {
                        temp.put(str, 1);
                    }
                }

                for (String str : temp.keySet()) {
                    if (map.containsKey(str)) {
                        map.put(str, temp.get(str) + map.get(str));
                    } else {
                        map.put(str, 1);
                    }
                }
                texts.put(index, resultLemmStop.toString());
                index++;
            }
            map = subMapByInteger(map);
            map = sortByValue(map);
            tezaurus.put(directory.getName(), new HashMap<>(map));
            map.clear();
        }

        Map<Integer, String> otherTexts = new HashMap<>();
        for (int i = 0, index = 0; i < NFolders; i++) {
            File directory = new File(folders[i]);
            File[] contents = directory.listFiles();
            for (int j = 4; j < 10; j++) {
                String text = new String(Files.readAllBytes(Objects.requireNonNull(contents)[j].toPath()));
                String output = ExudeData.getInstance().filterStoppingsKeepDuplicates(text);
                StringBuilder resultLemmStop = new StringBuilder();
                for (String str : stanfordLemmatizer.lemmatize(output)) {
                    resultLemmStop.append(" ").append(str);
                }

                otherTexts.put(index, resultLemmStop.toString());
                index++;
            }
        }

        List<Integer> indexXYZ = new ArrayList<>();
        for (int i = 0; i < NFolders; i++) {
            Map<String, Integer> temp;
            File directory = new File(folders[i]);
            temp = tezaurus.get(directory.getName());
            for (int j = 0; j < 12; j++) {
                int index = 0;
                for (String str : temp.keySet()) {
                    if (texts.get(j).toLowerCase().contains(str.toLowerCase())) {
                        index++;
                    }
                }
                indexXYZ.add(index);
            }
        }

        List<Integer> indexX = indexXYZ.subList(0, 12);
        List<Integer> indexY = indexXYZ.subList(12, 24);
        List<Integer> indexZ = indexXYZ.subList(24, 36);
        Map<Integer, List<Integer>> centers = new HashMap<>();
        centers.put(0, stanfordLemmatizer.centerOfClaster(indexX, indexY, indexZ, 0, 4));
        centers.put(1, stanfordLemmatizer.centerOfClaster(indexX, indexY, indexZ, 4, 8));
        centers.put(2, stanfordLemmatizer.centerOfClaster(indexX, indexY, indexZ, 8, 12));

        System.out.println(Arrays.toString(indexXYZ.toArray()) + " - " + indexXYZ.size());
        System.out.println(Arrays.toString(indexX.toArray()) + " - " + indexX.size());
        System.out.println(Arrays.toString(indexY.toArray()) + " - " + indexY.size());
        System.out.println(Arrays.toString(indexZ.toArray()) + " - " + indexZ.size());

        List<Integer> indexXYZ2 = new ArrayList<>();
        for (int i = 0; i < NFolders; i++) {
            Map<String, Integer> temp;
            File directory = new File(folders[i]);
            temp = tezaurus.get(directory.getName());
            for (int j = 0; j < 18; j++) {
                int index = 0;
                for (String str : temp.keySet()) {
                    if (otherTexts.get(j).toLowerCase().contains(str.toLowerCase())) {
                        index++;
                    }

                }
                indexXYZ2.add(index);
            }
        }

        List<Integer> indexX2 = indexXYZ2.subList(0, 18);
        List<Integer> indexY2 = indexXYZ2.subList(18, 36);
        List<Integer> indexZ2 = indexXYZ2.subList(36, 54);

        float a;
        Coord3d[] coord3ds = new Coord3d[33];
        Color[] colors = new Color[33];

        for (int i = 0; i < 12; i++) {
            coord3ds[i] = new Coord3d(indexX.get(i), indexY.get(i), indexZ.get(i));
            coord3ds[12] = new Coord3d(centers.get(0).get(0), centers.get(1).get(0), centers.get(2).get(0));
            colors[12] = new Color(0.1f, 0.1f, 0.1f);
            coord3ds[13] = new Coord3d(centers.get(0).get(1), centers.get(1).get(1), centers.get(2).get(1));
            colors[13] = new Color(0.1f, 0.1f, 0.1f);
            coord3ds[14] = new Coord3d(centers.get(0).get(2), centers.get(1).get(2), centers.get(2).get(2));
            colors[14] = new Color(0.1f, 0.1f, 0.1f);
        }
        for (int i = 0; i < 4; i++) {
            colors[i] = first;
            colors[i + 4] = second;
            colors[i + 8] = third;
        }

        for (int i = 0; i < 18; i++) {
            coord3ds[i + 15] = new Coord3d(indexX2.get(i), indexY2.get(i), indexZ2.get(i));
        }
        for (int i = 15; i < 21; i++) {
            colors[i] = first2;
            colors[i + 6] = second2;
            colors[i + 12] = third2;
        }

        colors=stanfordLemmatizer.setCordin(colors);
        // Create a drawable scatter with a colormap
        Scatter scatter1 = new Scatter(coord3ds, colors);
        scatter1.setWidth(15f);
        // Create a chart and add scatter
        Chart chart = new Chart();
        chart.getAxeLayout().setMainColor(Color.BLACK);
        chart.getView().setBackgroundColor(Color.WHITE);
        chart.getScene().add(scatter1);
        ChartLauncher.openChart(chart);
    }

    private List<Integer> centerOfClaster(List<Integer> indexX, List<Integer> indexY, List<Integer> indexZ, int from, int end) {
        List<Integer> center = new ArrayList<>();
        int temp = 0;
        int temp1 = 0;
        int temp2 = 0;
        int count = 0;
        for (int j = from; j < end; j++) {
            temp += indexX.get(j);
            temp1 += indexY.get(j);
            temp2 += indexZ.get(j);
            count++;
        }
        center.add(temp / count);
        center.add(temp1 / count);
        center.add(temp2 / count);
        return center;
    }
    private Color[] setCordin(Color[] colors){
        colors[15]=first3;
        colors[16]=first3;
        colors[17]=first3;
        colors[21]=second3;
        colors[32]=third3;
        return colors;
    }


}