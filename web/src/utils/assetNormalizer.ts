import type { Asset } from '../types/asset';

export function normalizeAssetRaw(raw: Record<string, unknown>, idx = 0): Asset {
  const read = (keys: string[]) => {
    for (const k of keys) {
      const v = raw[k];
      if (typeof v === 'string' || typeof v === 'number') return String(v);
    }
    return '';
  };

  const id = read(['id', '_id', 'assetTag', 'asset_tag', 'qrCodeId', 'qr_code_id']) || String(idx);
  const name = read(['name', 'assetName', 'asset_name']);
  const qr_code_id = read(['qr_code_id', 'qrCodeId', 'qr_code', 'qrCode']);
  const statusRaw = read(['status', 'state']).toLowerCase().replace(/\s+/g, '_');

  return {
    id,
    name,
    qr_code_id,
    serial_number: read(['serial_number', 'serialNumber']),
    // cast through unknown to avoid 'any' lint rule
    status: (statusRaw as unknown as Asset['status']) || 'in_service',
    location: read(['location', 'currentLocation']),
    last_inspection_date: read(['last_inspection_date', 'lastInspectionDate']) || null,
    next_service_date: null,
    warranty_expiration: null,
    custom_attributes_json: {},
    version: Number(raw.version ?? 0),
    created_at: String(raw.created_at ?? ''),
  };
}

export function normalizeAssetsResponse(resp: unknown): { items: Asset[]; total: number } {
  if (Array.isArray(resp)) return { items: resp as Asset[], total: resp.length };
  if (resp && typeof resp === 'object') {
    const o = resp as Record<string, unknown>;
    const content = o.content ?? o.data;
    if (Array.isArray(content)) {
      const items = content.map((r, i) => normalizeAssetRaw(r as Record<string, unknown>, i));
      const total =
        typeof o.totalElements === 'number'
          ? o.totalElements
          : typeof o.total === 'number'
            ? o.total
            : items.length;
      return { items, total };
    }
  }
  return { items: [], total: 0 };
}
