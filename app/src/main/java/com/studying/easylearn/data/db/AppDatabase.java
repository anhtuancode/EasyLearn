package com.studying.easylearn.data.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.studying.easylearn.data.dao.StoryDao;
import com.studying.easylearn.data.dao.UserDao;
import com.studying.easylearn.data.dao.UserStoryProgressDao;
import com.studying.easylearn.data.dao.UserWordDao;
import com.studying.easylearn.data.dao.VocabularyDao;
import com.studying.easylearn.data.entities.Story;
import com.studying.easylearn.data.entities.User;
import com.studying.easylearn.data.entities.UserStoryProgress;
import com.studying.easylearn.data.entities.UserWord;
import com.studying.easylearn.data.entities.Vocabulary;
import com.studying.easylearn.utils.AppExecutors;

import java.util.ArrayList;
import java.util.List;

@Database(
        entities = {User.class, Story.class, Vocabulary.class, UserWord.class, UserStoryProgress.class},
        version = 8,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract StoryDao storyDao();
    public abstract VocabularyDao vocabularyDao();
    public abstract UserWordDao userWordDao();
    public abstract UserStoryProgressDao userStoryProgressDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "easylearn_database"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            AppExecutors.getInstance().diskIO().execute(() -> {
                if (INSTANCE != null) {
                    // Tài khoản mẫu ban đầu
                    User testUser = new User(
                            "test@easylearn.com",
                            "12345678",
                            "Tuấn",
                            "LOCAL",
                            System.currentTimeMillis(),
                            1,
                            "A1",
                            3,
                            0,
                            ""
                    );
                    INSTANCE.userDao().insertUser(testUser);

                    // Nạp bộ 30 truyện phân cấp từ A1 đến C2
                    INSTANCE.storyDao().insertStories(getInitialStories());
                }
            });
        }
    };

    // Hàm tạo danh sách 30 truyện mẫu phân bổ đều các cấp độ
    private static List<Story> getInitialStories() {
        List<Story> list = new ArrayList<>();

        // --- CẤP ĐỘ A1 (BEGINNER) ---
        list.add(new Story("The Lost Kitten", "A small cat is crying in the park. A girl gives it some milk. The cat is happy now.", "Chú mèo lạc", "A1", "Đời sống", 2, true));
        list.add(new Story("My Morning Routine", "I wake up at 7 AM. I brush my teeth and wash my face. Then I eat bread and drink hot tea.", "Thói quen buổi sáng", "A1", "Đời sống", 2, false));
        list.add(new Story("A Day at the Beach", "The sun is hot. The water is blue. Children are building sandcastles. We eat ice cream and smile.", "Một ngày ở bãi biển", "A1", "Du lịch", 2, false));
        list.add(new Story("The Magic Pen", "Tom has a blue pen. When he draws a tree, the tree becomes real. He draws an apple and eats it.", "Cây bút thần kỳ", "A1", "Viễn tưởng", 3, false));
        list.add(new Story("Going to the Market", "Mom buys red apples, green cabbage, and fresh fish. The market is very noisy and crowded.", "Đi chợ", "A1", "Văn hóa", 3, false));

        // --- CẤP ĐỘ A2 (ELEMENTARY) ---
        list.add(new Story("A Strange Noise", "Late at night, Sarah heard a strange noise from the kitchen. She took a flashlight and walked slowly. It was just her dog eating a dropped sausage.", "Tiếng động lạ", "A2", "Kịch tính", 3, false));
        list.add(new Story("The Broken Bicycle", "David was riding to school when his tire went flat. He had to walk for two miles and arrived late for the math test.", "Chiếc xe đạp hỏng", "A2", "Đời sống", 3, false));
        list.add(new Story("The Secret Recipe", "Grandma's soup is always the best. Today, she taught me her secret: a pinch of ginger and a lot of love.", "Công thức bí mật", "A2", "Văn hóa", 4, false));
        list.add(new Story("A Trip to the Mountains", "We packed our tents and drove up the steep hill. The air was crisp, and we spent the night counting stars by the campfire.", "Chuyến đi núi", "A2", "Du lịch", 4, false));
        list.add(new Story("The Robot Helper", "Mr. Smith bought a small robot to clean his house. But the robot kept hiding his socks under the sofa.", "Người giúp việc robot", "A2", "Viễn tưởng", 3, false));

        // --- CẤP ĐỘ B1 (INTERMEDIATE) ---
        list.add(new Story("The Midnight Coffee", "It was raining heavily outside. Raindrops gently tapped on the clear glass window. Lucas sat quietly in the corner of a cozy coffee shop, enjoying the warm aroma of roasted beans. A mysterious stranger walked in, shook off a wet umbrella, and whispered softly to the waiter.", "Quán cà phê lúc nửa đêm", "B1", "Đời sống", 4, false));
        list.add(new Story("The Clockmaker's Promise", "Every clock in Master Julian's workshop ticked in perfect harmony, except for one silver pocket watch on the top shelf. It had stopped at exactly midnight twenty years ago.", "Lời hứa của người thợ đồng hồ", "B1", "Kịch tính", 5, false));
        list.add(new Story("Escaping the City", "Tired of the endless traffic jams and neon lights, Maya decided to quit her corporate job. She moved to a remote village to start an organic farm.", "Trốn khỏi thành phố", "B1", "Đời sống", 4, false));
        list.add(new Story("The Forgotten Museum", "While exploring the narrow alleys of Venice, Paul stumbled upon an abandoned museum. The paintings inside seemed to change expressions when he wasn't looking.", "Bảo tàng bị lãng quên", "B1", "Kỳ bí", 5, false));
        list.add(new Story("A Cultural Misunderstanding", "When visiting Tokyo, Alex tried to tip the waiter for excellent service. To his surprise, the waiter politely refused, explaining that good service is just the standard.", "Hiểu lầm văn hóa", "B1", "Văn hóa", 4, false));

        // --- CẤP ĐỘ B2 (UPPER-INTERMEDIATE) ---
        list.add(new Story("Echoes of the Past", "The abandoned mansion stood as a testament to a forgotten era. Its crumbling Victorian architecture harbored secrets that historians had spent decades trying to unravel.", "Tiếng vọng quá khứ", "B2", "Lịch sử", 6, false));
        list.add(new Story("The AI Dilemma", "As artificial intelligence seamlessly integrated into daily healthcare, Dr. Aris faced an ethical paradox: trust the machine's flawless diagnosis or her own clinical intuition.", "Nghịch lý AI", "B2", "Khoa học", 6, false));
        list.add(new Story("Sustainable Horizons", "The initiative aimed to transform the barren wasteland into a self-sustaining ecosystem. Utilizing advanced hydroponics, the community achieved food independence within a mere five years.", "Chân trời bền vững", "B2", "Đời sống", 5, false));
        list.add(new Story("The Art of Negotiation", "During the high-stakes corporate merger, Elena maintained a composed demeanor. Her ability to read subtle micro-expressions ultimately secured a highly lucrative deal for her firm.", "Nghệ thuật đàm phán", "B2", "Kinh doanh", 6, false));
        list.add(new Story("A Symphony in Vienna", "The grand concert hall was filled with palpable anticipation. As the conductor raised his baton, a wave of profound emotion washed over the audience, transcending language barriers.", "Bản giao hưởng ở Vienna", "B2", "Nghệ thuật", 5, false));

        // --- CẤP ĐỘ C1 (ADVANCED) ---
        list.add(new Story("Quantum Entanglement", "The paradigm shift brought about by quantum mechanics fundamentally altered our comprehension of causality, suggesting that subatomic particles remain inextricably linked across vast cosmic distances.", "Sự vướng víu lượng tử", "C1", "Khoa học", 7, false));
        list.add(new Story("Philosophical Musings", "Existentialism posits that individuals are entirely responsible for bestowing meaning upon their own lives, navigating an inherently absurd universe without pre-ordained destiny.", "Suy ngẫm triết học", "C1", "Triết học", 7, false));
        list.add(new Story("The Geopolitical Chessboard", "Navigating the labyrinthine intricacies of international diplomacy requires a nuanced understanding of historical grievances and contemporary economic interdependence.", "Bàn cờ địa chính trị", "C1", "Chính trị", 8, false));
        list.add(new Story("Linguistic Evolution", "Language is a dynamic, ever-evolving entity. The assimilation of loanwords and the spontaneous generation of colloquialisms reflect the continuous cultural metamorphosis of a society.", "Sự tiến hóa ngôn ngữ", "C1", "Văn hóa", 7, false));
        list.add(new Story("Architectural Marvels", "The juxtaposition of brutalist concrete structures against the ephemeral beauty of meticulously curated botanical gardens creates a striking aesthetic dichotomy in the urban landscape.", "Kỳ quan kiến trúc", "C1", "Nghệ thuật", 8, false));

        // --- CẤP ĐỘ C2 (MASTERY) ---
        list.add(new Story("The Epistemological Crisis", "In an era characterized by the ubiquitous proliferation of hyper-realistic deepfakes, society is grappling with a profound epistemological crisis, fundamentally undermining our collective consensus on empirical reality.", "Cuộc khủng hoảng nhận thức luận", "C2", "Xã hội", 9, false));
        list.add(new Story("Neuroplasticity and Trauma", "Cutting-edge neurobiological research elucidates the brain's remarkable neuroplasticity, revealing intricate mechanisms through which targeted cognitive behavioral interventions can rewire neural pathways entrenched by severe psychological trauma.", "Tính dẻo thần kinh và chấn thương", "C2", "Y học", 10, false));
        list.add(new Story("Macroeconomic Volatility", "The intricate interplay between inflationary pressures, quantitative easing policies, and supply chain bottlenecks has precipitated a period of unprecedented macroeconomic volatility, confounding traditional economic forecasting models.", "Sự biến động kinh tế vĩ mô", "C2", "Kinh tế", 9, false));
        list.add(new Story("Astrophysical Anomalies", "The detection of anomalous gravitational waves emanating from the periphery of the observable universe challenges the prevailing cosmological standard model, necessitating a rigorous reevaluation of general relativity.", "Các dị thường vật lý thiên văn", "C2", "Vũ Vũ trụ học", 10, false));
        list.add(new Story("The Aesthetic of Melancholy", "Within the canon of Romantic literature, melancholy is not merely portrayed as an affliction, but rather elevated to a sublime aesthetic experience, serving as a conduit for profound introspection and transcendental revelation.", "Mỹ học của sự u sầu", "C2", "Văn học", 9, false));

        return list;
    }
}