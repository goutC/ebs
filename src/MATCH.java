import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MATCH{
	public static void main(String[] args) {
		MatchPoolPlayerInfo p1 = new MatchPoolPlayerInfo(12,15);
		MatchPoolPlayerInfo p2 = new MatchPoolPlayerInfo(13,17);
		MatchPoolPlayerInfo p3 = new MatchPoolPlayerInfo(14,18);
		MatchPoolPlayerInfo p4 = new MatchPoolPlayerInfo(15,100);
		putPlayerIntoMatchPool(p1.getPlayerId(),p2.getRank());
		putPlayerIntoMatchPool(p2.getPlayerId(),p2.getRank());
		putPlayerIntoMatchPool(p3.getPlayerId(),p3.getRank());
		putPlayerIntoMatchPool(p4.getPlayerId(),p4.getRank());
	}
/**
     * ƥ���߳�
     */
    private static ScheduledExecutorService sec = Executors.newSingleThreadScheduledExecutor();
  
    /**
     * ÿ������Ҫƥ�䵽���������
     */
    private static int NEED_MATCH_PLAYER_COUNT = 1;
    /**
     * ƥ���
     */
    private static ConcurrentHashMap<Integer,MatchPoolPlayerInfo> playerPool = new ConcurrentHashMap<Integer,MatchPoolPlayerInfo>();


    static{
        sec.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                matchProcess(playerPool);
            }
        }, 1,5, TimeUnit.SECONDS);//ÿ��1��ƥ��һ��
    }

     /**
     * ����ҷ���ƥ���
     * @param playerId
     * @param rank
     * @return
     */
    public static void putPlayerIntoMatchPool(int playerId, int rank){
        MatchPoolPlayerInfo playerInfo = new MatchPoolPlayerInfo(playerId, rank);
        playerPool.put(playerId, playerInfo);
    }

    /**
     * ����Ҵ�ƥ����Ƴ�
     * @param playerId
     */
    public static void removePlayerFromMatchPool(int playerId){
        playerPool.remove(playerId);
    }

    private static void matchProcess(ConcurrentHashMap<Integer,MatchPoolPlayerInfo> playerPool) {
        long startTime = System.currentTimeMillis();
        System.out.println("ִ��ƥ�俪ʼ|��ʼʱ��|"+startTime);
        try{
            //�Ȱ�ƥ����е���Ұ������ֲ�
            TreeMap<Integer,HashSet<MatchPoolPlayerInfo>> pointMap = new TreeMap<Integer,HashSet<MatchPoolPlayerInfo>>();
            for (MatchPoolPlayerInfo matchPlayer : playerPool.values()) {
                //��ƥ�������ʱ��̫����ֱ���Ƴ�
                if((System.currentTimeMillis()-matchPlayer.getStartMatchTime())>60 * 60 * 1000){
//                    log.warn(matchPlayer.getPlayerId()+"��ƥ�������ʱ�䳬��һ��Сʱ��ֱ���Ƴ�");
                    removePlayerFromMatchPool(matchPlayer.getPlayerId());
                    continue;
                }
               HashSet<MatchPoolPlayerInfo> set = pointMap.get(matchPlayer.getRank());
                if(set==null){
                    set = new HashSet<MatchPoolPlayerInfo>();
                    set.add(matchPlayer);
                    pointMap.put(matchPlayer.getRank(), set);
                }else{
                    set.add(matchPlayer);
                }
            }

            for (HashSet<MatchPoolPlayerInfo> sameRankPlayers: pointMap.values()) {
                boolean continueMatch = true;
                while(continueMatch){
                    //�ҳ�ͬһ��������ȴ�ʱ�������ң�������ƥ�䣬��Ϊ�����������
                    //�����������ƥ�䵽���ȴ�ʱ������̵���Ҹ�ƥ�䲻��
                    MatchPoolPlayerInfo oldest = null;
                    for (MatchPoolPlayerInfo playerMatchPoolInfo : sameRankPlayers) {
                        if(oldest==null){
                            oldest = playerMatchPoolInfo;
                        }else if(playerMatchPoolInfo.getStartMatchTime()<oldest.getStartMatchTime()){
                            oldest = playerMatchPoolInfo;
                        }
                    }
                    if(oldest==null){
                        break;
                    }
                    System.out.println(oldest.getPlayerId()+"|Ϊ�÷����ϵȴ����ʱ�����ҿ�ʼƥ��|rank|"+oldest.getRank());

                    long now = System.currentTimeMillis();
                    int waitSecond = (int)((now-oldest.getStartMatchTime())/1000);

                    System.out.println(oldest.getPlayerId()+"|��ǰʱ���Ѿ��ȴ���ʱ��|waitSecond|"+waitSecond+"|��ǰϵͳʱ��|"+now+"|��ʼƥ��ʱ��|"+oldest.getStartMatchTime());

                    //���ȴ�ʱ������ƥ�䷶Χ
                    float c2 = 1.5f;
                    int c3 = 5;
                    int c4 = 100;

                    float u = (float) Math.pow(waitSecond, c2);
                    u = u + c3;
                    u = (float) Math.round(u);
                    u = Math.min(u, c4);

                    int min = (oldest.getRank() - (int)u)<0?0:(oldest.getRank() - (int)u);
                    int max = oldest.getRank() + (int)u;

                    System.out.println(oldest.getPlayerId()+"|��������rank��Χ����|"+min+"|rank��Χ����|"+max);

                    int middle = oldest.getRank();

                    List<MatchPoolPlayerInfo> matchPoolPlayer = new ArrayList<MatchPoolPlayerInfo>();
                    //����λ������������Χ����
                    for(int searchRankUp = middle,searchRankDown = middle; searchRankUp <= max||searchRankDown>=min;searchRankUp++,searchRankDown--){
//                        HashSet<MatchPoolPlayerInfo> thisRankPlayers = pointMap.getOrDefault(searchRankUp,new HashSet<MatchPoolPlayerInfo>());
                    	HashSet<MatchPoolPlayerInfo> thisRankPlayers =  pointMap.get(searchRankUp);
                    	if(thisRankPlayers == null )
                    		thisRankPlayers = new HashSet<MatchPoolPlayerInfo>();
                        if(searchRankDown!=searchRankUp&&searchRankDown>0){
                            thisRankPlayers.addAll(pointMap.get(searchRankDown)==null?new HashSet<MatchPoolPlayerInfo>():pointMap.get(searchRankDown));
                        }
                        if(!thisRankPlayers.isEmpty()){
                            if(matchPoolPlayer.size()<NEED_MATCH_PLAYER_COUNT){
                                Iterator<MatchPoolPlayerInfo> it = thisRankPlayers.iterator();  
                                while (it.hasNext()) {
                                    MatchPoolPlayerInfo player = it.next();
                                    if(player.getPlayerId()!=oldest.getPlayerId()){//�ų���ұ���
                                        if(matchPoolPlayer.size()<NEED_MATCH_PLAYER_COUNT){
                                            matchPoolPlayer.add(player);
                                            System.out.println(oldest.getPlayerId()+"|ƥ�䵽���|"+player.getPlayerId()+"|rank|"+player.getRank());
                                            //�Ƴ�
                                            it.remove();
                                        }else{
                                            break;
                                        }
                                    }
                                }
                            }else{
                                break;
                            }
                        }
                    }

                    if(matchPoolPlayer.size()==NEED_MATCH_PLAYER_COUNT){
                        System.out.println(oldest.getPlayerId()+"|ƥ�䵽�����������|�ύƥ��ɹ�����");
                        //�Լ�Ҳƥ����Ƴ�
                        sameRankPlayers.remove(oldest);
                        //ƥ��ɹ�����
                        matchPoolPlayer.add(oldest);
                        //TODO ����Ե����ύƥ��ɹ�����
                        //matchSuccessProcess(matchPoolPlayer);
                    }else{
                        //�������εȴ�ʱ�������Ҷ�ƥ�䲻�������������ó�����
                        continueMatch = false;
                        System.out.println(oldest.getPlayerId()+"|ƥ�䵽�������������ȡ������ƥ��");
                        //�黹ȡ���������
                        for(MatchPoolPlayerInfo player:matchPoolPlayer){
                            HashSet<MatchPoolPlayerInfo> sameRankPlayer = pointMap.get(player.getRank());
                            sameRankPlayer.add(player);
                        }
                    }
                }
            }
        }catch(Throwable t){
//            log.error("match|error",t);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("ִ��ƥ�����|����ʱ��|"+endTime+"|��ʱ|"+(endTime-startTime)+"ms");
    }

    private static class MatchPoolPlayerInfo{
        private int playerId;//���ID
        private int rank;//��ҷ���
        private long startMatchTime;//��ʼƥ��ʱ��


        private MatchPoolPlayerInfo(int playerId, int rank) {
            super();
            this.playerId = playerId;
            this.rank = rank;
            this.startMatchTime = System.currentTimeMillis();
        }

        public int getPlayerId() {
            return playerId;
        }

        public int getRank() {
            return rank;
        }

        public long getStartMatchTime() {
            return startMatchTime;
        }
    }
    
} 