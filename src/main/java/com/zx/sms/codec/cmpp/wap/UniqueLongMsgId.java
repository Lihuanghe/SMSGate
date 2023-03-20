package com.zx.sms.codec.cmpp.wap;

import java.io.Serializable;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.connect.manager.EndpointEntity;

import io.netty.channel.Channel;

public class UniqueLongMsgId implements Serializable {
	private final Logger logger = LoggerFactory.getLogger(UniqueLongMsgId.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String entityId;
	private String channelId;
	private SocketAddress remoteAddr;
	private SocketAddress localAddr;
	private long timestamp;
	private int sequenceNo;
	private int pkseq;
	private short pktotal;
	private short pknumber;
	private boolean createdByRead;

	// 只有从channel接收消息时才用
	private String cacheKey;
	private boolean recvLongMsgOnMultiLink = false;

	// 限制外部业务类创建该ID,该ID只在长短信合并时生成
	UniqueLongMsgId(String id) {
		this.id = id;
	}

	// 一个长短信的标识，从哪里来的：什么时间，从哪个账号的哪个连接接收上来的
	// 或者是发送给哪个账号，发个连接出去的
	UniqueLongMsgId(EndpointEntity entity, Channel ch, LongSMSMessage lmsg, long lId,boolean createdByRead) {
		String srcIdAndDestId = lmsg.getSrcIdAndDestId();

		this.entityId = entity != null && entity.getId() != null ? entity.getId() : "";
		this.recvLongMsgOnMultiLink = entity != null ? entity.isRecvLongMsgOnMultiLink() : false;
		LongMessageFrame frame =  lmsg.generateFrame() ;
		FrameHolder fh = LongMessageFrameHolder.INS.parseFrameKey(frame);
		if (lmsg.getUniqueLongMsgId() == null) {
			// 账号
			StringBuffer keysb = new StringBuffer(entityId);

			// channel
			if (ch != null && (entity == null || !entity.isRecvLongMsgOnMultiLink())) {
				keysb.append(".").append(ch.id().asShortText());
			}
			// 手机号，端口号
			keysb.append(".").append(srcIdAndDestId);

			if (fh != null && fh.getTotalLength() > 1) {
				// 分片ID,及分片总数
				keysb.append(".").append(fh.frameKey).append(".").append(fh.getTotalLength());
				// 只有是从channel上接收时，因为有合并的问题，才拼接唯一序号
				if(createdByRead) {
					this.cacheKey = keysb.toString();
					Long longId = LongMessageFrameHolder.INS.getUniqueLongMsgId(cacheKey, recvLongMsgOnMultiLink);
					// 唯一ID
					keysb.append(".").append(longId);
				}else {
					keysb.append(".").append(lId);
				}

			} else {
				// 唯一ID
				keysb.append(".").append(DefaultSequenceNumberUtil.getSequenceNo());
			}
			this.createdByRead = createdByRead;
			this.id = keysb.toString();
		} else {
			this.id = lmsg.getUniqueLongMsgId().getId();
			this.createdByRead = lmsg.getUniqueLongMsgId().isCreatedByRead();
		}

		if (ch != null) {
			this.channelId = ch.id().asShortText();
			this.remoteAddr = ch.remoteAddress();
			this.localAddr = ch.localAddress();
		}

		this.timestamp = ((BaseMessage) lmsg).getTimestamp();
		this.sequenceNo = ((BaseMessage) lmsg).getSequenceNo();
		this.pknumber = frame.getPknumber();
		this.pktotal = frame.getPktotal();
		this.pkseq = frame.getPkseq();

	}

	UniqueLongMsgId(EndpointEntity entity, LongSMSMessage lmsg) {
		this(entity, null, lmsg,DefaultSequenceNumberUtil.getSequenceNo(), false);
	}
	
	public UniqueLongMsgId(UniqueLongMsgId id,  Channel ch) {
		this.id = id.getId();
		this.entityId = id.getEntityId();
		this.channelId = ch.id().asShortText();
		this.remoteAddr = ch.remoteAddress();
		this.localAddr = ch.localAddress();
		this.createdByRead = id.isCreatedByRead();
		this.recvLongMsgOnMultiLink = id.isRecvLongMsgOnMultiLink();
		this.timestamp = id.getTimestamp();
		this.sequenceNo = id.getSequenceNo();
		this.pknumber = id.getPknumber();
		this.pktotal = id.getPktotal();
		this.pkseq = id.getPkseq();
	}

	UniqueLongMsgId(UniqueLongMsgId id, LongMessageFrame frame) {
		this.id = id.getId();
		this.entityId = id.getEntityId();
		this.channelId = id.getChannelId();
		this.remoteAddr = id.getRemoteAddr();
		this.localAddr = id.getLocalAddr();
		this.createdByRead = id.isCreatedByRead();
		this.recvLongMsgOnMultiLink = id.isRecvLongMsgOnMultiLink();
		this.timestamp = frame.getTimestamp();
		this.sequenceNo = frame.getSequence();
		this.pknumber = frame.getPknumber();
		this.pktotal = frame.getPktotal();
		this.pkseq = frame.getPkseq();
	}

	public String getId() {
		return id;
	}

	public String getEntityId() {
		return entityId;
	}

	public SocketAddress getRemoteAddr() {
		return remoteAddr;
	}

	public SocketAddress getLocalAddr() {
		return localAddr;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getSequenceNo() {
		return sequenceNo;
	}

	public int getPkseq() {
		return pkseq;
	}

	public short getPktotal() {
		return pktotal;
	}

	public short getPknumber() {
		return pknumber;
	}

	public String getChannelId() {
		return channelId;
	}

	public boolean isCreatedByRead() {
		return createdByRead;
	}

	
	public boolean isRecvLongMsgOnMultiLink() {
		return recvLongMsgOnMultiLink;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UniqueLongMsgId other = (UniqueLongMsgId) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UniqueLongMsgId [id=" + id + ", entityId=" + entityId +  ", timestamp=" + timestamp + ", sequenceNo=" + sequenceNo
				+ ", pkseq=" + pkseq + ", pktotal=" + pktotal + ", pknumber=" + pknumber +", channelId=" + channelId + ", remoteAddr="
						+ remoteAddr + ", localAddr=" + localAddr + ", createdByRead="
				+ createdByRead + "]";
	}

	void clearCacheKey() {
		if(cacheKey!=null)
			LongMessageFrameHolder.INS.clearUniqueLongMsgIdCacheKey(cacheKey, recvLongMsgOnMultiLink);
	}
}
