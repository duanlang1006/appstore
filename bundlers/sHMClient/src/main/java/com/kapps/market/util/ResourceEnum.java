package com.kapps.market.util;

/**
 * 2010-7-19<br>
 * ��ѯ��صĳ���<br>
 * �����˶�Ӧ
 * 
 * @author admin
 * 
 */
public interface ResourceEnum {

	public static final int INVALID = -11;

	// ------------------Ӧ�����/�շ�
	// ȫ���������շѺ����
	public static final int FEE_NONE_TYPE = 0;
	// �շ�
	public static final int FEE_CHARGE_TYPE = 1;
	// ���
	public static final int FEE_FREE_TYPE = 2;

	// ------------------Ӧ����������
	// ���������н���
	public static final int SORT_DALL_NUM_DESC = 1;
	// �ϴ�ʱ�併��
	public static final int SORT_ADD_TIME_DESC = 2;
	// �û����� des(�Ȱ������汾�Ǽ������ٰ�����������)
	public static final int SORT_USER_RATING = 3;
	// ������ des(����������������������������)
	public static final int SORT_DAY_DOWNLOAD = 4;
	// ������ des(����������������������������)
	public static final int SORT_WEEK_DOWNLOAD = 5;
	// ������ des(����������������������������)
	public static final int SORT_MONTH_DOWNLOAD = 6;
	// ������des(����������������������������)
	public static final int SORT_YEAR_DOWNLOAD = 7;

	// --------------------������ľٱ� RE
	// ɫ������
	public static final int RE_EROTICISM = 1;
	// ����ͼƬ
	public static final int RE_VIOLENCE = 2;
	// �����������ݻ򹥻�������
	public static final int RE_REBARBATIVE = 3;
	// ���ֻ������к�
	public static final int RE_DELETERIOUS = 4;
	// ���������
	public static final int RE_OTHER_REASON = 5;

	// ------------------��������۵ı��
	// ���õ�
	public static final int COMMENT_MARK_USEFUL = 0;
	// ����
	public static final int COMMENT_MARK_USELESS = 1;

	// -------------------�����ѯ����
	public static final int SEARCH_ALL = 0;
	// �����������
	public static final int SEARCH_NAME = 1;
	// ��������
	public static final int SEARCH_AUTHOR = 2;
	// �����������
	public static final int SEARCH_INTRODUCE = 3;
	// ���Ұ���
	public static final int SEARCH_PACKAGE = 4;

	// -------------------�����������
	// ��ͨ���һ�������ʽ�У��շ����ţ�������ţ������ϴ�
	public static final int CATEGORY_TYPE_COMMON = 1;
	// ��ѡ�����롣ѧϰ�ر�������ر��ȣ�һ��ֱ�����
	public static final int CATEGORY_TYPE_PICKED = 2;

	// --------------------�������--------------
	// Ӧ��
	public static final int QUERY_CATETORY_APPLICATION = 1;
	// ��Ϸ
	public static final int QUERY_CATETORY_GAME = 2;
	// �Ƽ����
	public static final int QUERY_CATETORY_PICK = 3;

	// --------------------�������������-----------
	// �ö���棺 �������ʱѡ����ö�������
	public static final int AD_TYPE_TOP = 0;
	// �����Ƽ��� �ɹ���Ա����ѡȡ���������
	public static final int AD_TYPE_EXCEL = 1;

}
